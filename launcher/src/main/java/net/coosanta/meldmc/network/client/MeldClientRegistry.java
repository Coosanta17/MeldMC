package net.coosanta.meldmc.network.client;

import net.coosanta.meldmc.minecraft.ServerInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MeldClientRegistry {
    private static final ConcurrentMap<String, MeldClient> CLIENTS = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached {@link MeldClient} for the given {@link ServerInfo}, or creates and caches a new one.
     * <p>
     * Uses the meld address if specified, otherwise falls back to the server address.
     *
     * @param info server info
     * @return cached or newly created {@link MeldClient} for the server
     */
    public static MeldClient getOrCreateClient(ServerInfo info) {
        return CLIENTS.computeIfAbsent(info.getAddress(), key -> {
            String queryAddress = info.getMeldAddress();
            String host;
            int port;
            boolean https = info.isHttps();
            boolean selfSigned = info.isSelfSigned();

            if (queryAddress != null && !queryAddress.equals("0.0.0.0:0")) {
                if (queryAddress.startsWith("https://")) {
                    https = true;
                    queryAddress = queryAddress.substring(8);
                } else if (queryAddress.startsWith("http://")) {
                    queryAddress = queryAddress.substring(7);
                }

                String[] parts = queryAddress.split(":");
                host = parts[0].equals("0.0.0.0") ? info.getAddress() : parts[0];
                port = parts.length > 1 ? Integer.parseInt(parts[1].split("/")[0]) : 80;
            } else {
                throw new IllegalArgumentException("Meld address is not configured for server: " + info.getName());
            }

            return new MeldClientImpl(host, port, https, selfSigned);
        });
    }

    /**
     * Gets the cached {@link MeldClient} for the specified address, or null if not present.
     *
     * @param address minecraft server address - not Meld address.
     * @return cached {@link MeldClient} or null
     */
    public static MeldClient getClient(String address) {
        return CLIENTS.get(address);
    }
}
