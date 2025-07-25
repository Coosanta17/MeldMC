package net.coosanta.meldmc.network.client;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record MeldData(String mcVersion, ModLoader modLoader, String modLoaderVersion,
                       Map<String, ClientMod> modMap) { // Todo: hash verifying
    public enum ModLoader {
        VANILLA,
        FORGE,
        NEOFORGE,
        FABRIC,
        QUILT
    }

    public record ClientMod(String modVersion, String hash, @Nullable String url, String filename, String modname, String modId,
                            String authors, String description) {
    }
}
