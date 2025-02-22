package net.coosanta.totalityloader.network;

import net.coosanta.totalityloader.minecraft.ServerInfo;
import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class Pinger {
    private static final Logger log = LoggerFactory.getLogger(Pinger.class);

    public static CompletableFuture<Void> ping(ServerInfo serverInfo) {
        log.info("Pinging {}", serverInfo.getAddress());
        CompletableFuture<Void> future = new CompletableFuture<>();

        InetSocketAddress address = getAddress(serverInfo.getAddress());
        MinecraftProtocol protocol = new MinecraftProtocol();

        ClientSession client = ClientNetworkSessionFactory.factory()
                .setRemoteSocketAddress(address)
                .setProtocol(protocol)
                .create();

        client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (session, info) -> {
            serverInfo.addStatusInfo(info);
//            log.info("Received server info {}", serverInfo.getDescription());
        });
        client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (session, pingTime) -> {
            serverInfo.setPing(pingTime);
//            log.info("Received ping {}", serverInfo.getPing());
            if (serverInfo.getPing() > 0) {
                serverInfo.setStatus(ServerInfo.Status.SUCCESSFUL);
            } else {
                serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
            }
            future.complete(null);
        });

        serverInfo.setStatus(ServerInfo.Status.PINGING);

        try {
            client.connect(true);
        } catch (Exception e) {
            serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
            future.completeExceptionally(e);
        }

        return future;
    }

    private static @NotNull InetSocketAddress getAddress(String addressAndPort) {
        int port = 25565;
        String[] parts = addressAndPort.split(":");

        if (parts.length > 2) throw new IllegalArgumentException("Invalid address and port format in: " + addressAndPort);
        else if (parts.length == 2) {
            try {
                port = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number: " + parts[1] + " in address: " + addressAndPort, e);
            }
        }

        String host = parts[0];

        return new InetSocketAddress(host, port);
    }

    public static void main(String[] args) {
        log.info("pinging...");
        ServerInfo info = new ServerInfo("Test", "mc.hypixel.net");
        ping(info).thenAccept((unused -> {
            log.info("Pingified!");
            log.info(info.toString());
        })).join();
    }
}
