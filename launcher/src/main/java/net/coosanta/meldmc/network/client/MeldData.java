package net.coosanta.meldmc.network.client;

import java.util.Map;

public record MeldData(String mcVersion, ModLoader modLoader, String modLoaderVersion, Map<String, ClientMod> modMap) {
    public enum ModLoader {
        VANILLA,
        FABRIC,
        QUILT,
        FORGE,
        NEOFORGE
    }

    public record ClientMod(String hash, String url, String filename) {
    }
}
