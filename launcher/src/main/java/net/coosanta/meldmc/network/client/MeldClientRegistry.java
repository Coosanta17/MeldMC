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
            String host = info.getMeldAddress().equals("0.0.0.0") ? info.getAddress() : info.getMeldAddress();
            return new MeldClientImpl(host, info.getMeldPort(), info.isHttps(), info.isSelfSigned());
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
