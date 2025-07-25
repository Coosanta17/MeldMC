package net.coosanta.meldmc.network;

import javafx.application.Platform;
import net.coosanta.meldmc.gui.controllers.serverselection.ServerEntry;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.network.client.MeldClient;
import net.coosanta.meldmc.network.client.MeldClientImpl;
import net.coosanta.meldmc.network.data.MeldCodec;
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

public class Pinger {
    private static final Logger log = LoggerFactory.getLogger(Pinger.class);

    public static void ping(ServerInfo serverInfo, ServerEntry serverEntry) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        InetSocketAddress address = getAddress(serverInfo.getAddress());
        MinecraftProtocol protocol = new MinecraftProtocol(MeldCodec.CODEC);

        ClientSession client = ClientNetworkSessionFactory.factory()
                .setRemoteSocketAddress(address)
                .setProtocol(protocol)
                .create();

//        client.addListener(new MeldClientListener(HandshakeIntent.STATUS));

        client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (session, info) -> {
            serverInfo.addStatusInfo(info);
            if (serverInfo.isMeldSupported()) { // TODO: REMOVE DEBUG
                MeldClient meldClient = new MeldClientImpl((serverInfo.getMeldAddress().equals("0.0.0.0")) ? address.getHostName() : serverInfo.getMeldAddress(), serverInfo.getMeldPort(), serverInfo.isHttps(), serverInfo.isSelfSigned());
                meldClient.fetchModInfo()
                        .thenAccept(serverInfo::setMeldData)
                        .exceptionally(e -> {
                            log.error("Failed to fetch mod info", e);
                            return null;
                        });
            }
        });

        client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (session, pingTime) -> {
            serverInfo.setPing(pingTime);
            // FIXME: If you have such fast internet that takes less than 0.5ms to ping the server then it will think the server is offline due to rounding (Math.ceil exists?????).
            if (pingTime > 0) {
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

        future.orTimeout(10, TimeUnit.SECONDS)
                .thenAccept(unused ->
                        Platform.runLater(serverEntry::updateComponents)
                )
                .exceptionally(e -> {
                    if (e instanceof TimeoutException || e.getCause() instanceof TimeoutException) {
                        serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
                        client.disconnect("Ping timed out");
                        log.warn("Server ping timed out after 10 seconds: {}", serverInfo.getAddress());
                    } else {
                        serverInfo.setStatus(ServerInfo.Status.UNREACHABLE);
                        log.error("Error pinging server {}: {}", serverInfo.getAddress(), e.getMessage(), e);
                    }
                    Platform.runLater(serverEntry::updateComponents);
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
}
