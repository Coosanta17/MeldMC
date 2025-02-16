package net.coosanta.totalityloader.network;

import net.coosanta.totalityloader.minecraft.ServerInfo;
import org.geysermc.mcprotocollib.network.ClientSession;
import org.geysermc.mcprotocollib.network.factory.ClientNetworkSessionFactory;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Pinger {
    private static final Logger log = LoggerFactory.getLogger(Pinger.class);

    public static ServerInfo ping(String host, int port) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        MinecraftProtocol protocol = new MinecraftProtocol();

        // Create a client session that will connect to the remote server
        ClientSession client = ClientNetworkSessionFactory.factory()
                .setRemoteSocketAddress(address)
                .setProtocol(protocol)
                .create();

        // Optional: If you need to handle authentication, set up your SessionService
        // client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, new SessionService());

        ServerInfo serverInfo = new ServerInfo(host, host);

        client.setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (session, info) -> {
            serverInfo.addStatusInfo(info);
        });

        client.setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, (session, pingTime) -> {
            serverInfo.setPing(pingTime);
        });

        System.out.println("connecting...");
        client.connect();
//        System.out.println(client.isConnected());

        // Blocks thread until client disconnects
        boolean init = true;
        while (client.isConnected() || init) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for ping response.", e);
                Thread.currentThread().interrupt();
            }
            System.out.print("e");
            init = false;
        }
        System.out.println("Returning object");
        return serverInfo;
    }

    public static void main(String[] args) {
        // Replace with the remote server's address and port
        System.out.println("pinging...");
        ServerInfo info = ping("play.squirtlesquadmc.com", 25565);
        System.out.println("Pingified!");
        System.out.println(info);
    }
}
