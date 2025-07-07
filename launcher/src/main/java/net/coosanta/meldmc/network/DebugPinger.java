package net.coosanta.meldmc.network;

import net.coosanta.meldmc.minecraft.ServerInfo;
import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DebugPinger {
    private static final Logger log = LoggerFactory.getLogger(DebugPinger.class);

    public static CompletableFuture<Void> ping(ServerInfo serverInfo) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        InetSocketAddress address = getAddress(serverInfo.getAddress());
        MinecraftProtocol protocol = new MinecraftProtocol();

        ClientSession client = ClientNetworkSessionFactory.factory()
                .setRemoteSocketAddress(address)
                .setProtocol(protocol)
                .create();

        client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (session, info) ->
                serverInfo.addStatusInfo(info));

        client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (session, pingTime) -> {
            serverInfo.setPing(pingTime);
            if (serverInfo.getPing() > 0) {
                serverInfo.setStatus(ServerInfo.Status.SUCCESSFUL);
            } else {
                serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
            }
            future.complete(null);
        });

        serverInfo.setStatus(ServerInfo.Status.PINGING);

        try {
            client.connect(false);
        } catch (Exception e) {
            serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
            future.completeExceptionally(e);
        }

        return future.orTimeout(10, TimeUnit.SECONDS)
                .thenAccept(unused ->
                        log.info("Ping completed successfully for server: {}", serverInfo.getAddress()))
                .exceptionally(e -> {
                    if (e instanceof TimeoutException || e.getCause() instanceof TimeoutException) {
                        client.disconnect("Ping timed out");
                        log.warn("Server ping timed out after 10 seconds: {}", serverInfo.getAddress());
                    } else {
                        log.error("Error pinging server {}: {}", serverInfo.getAddress(), e.getMessage(), e);
                    }
                    serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
                    return null;
                });
    }

    private static @NotNull InetSocketAddress getAddress(String addressAndPort) {
        int port = 25565;
        String[] parts = addressAndPort.split(":");

        if (parts.length > 2)
            throw new IllegalArgumentException("Invalid address and port format in: " + addressAndPort);
        else if (parts.length == 2) {
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number: " + parts[1] + " in address: " + addressAndPort, e);
            }
        }

        String host = parts[0];

        return InetSocketAddress.createUnresolved(host, port);
    }


    public static void main(String[] args) {
        ServerInfo srvRecord = new ServerInfo("SRV Record", "minecraft.coosanta.net");
        CompletableFuture<Void> future1 = ping(srvRecord);

        ServerInfo directIp = new ServerInfo("Direct IP", "121.223.134.136");
        CompletableFuture<Void> future2 = ping(directIp);

        CompletableFuture.allOf(future1, future2).join();

        log.info("SRV Record server information: {}", srvRecord);
        log.info("Direct IP server information: {}", directIp);
        log.info("All pings completed");
    }
}
