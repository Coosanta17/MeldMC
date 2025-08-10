package net.coosanta.meldmc.network.client;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
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

    public record ClientMod(String modVersion, String hash, @Nullable String url, @Nullable String projectUrl,
                            @Nullable String projectId, String filename, String modname, String modId, String authors,
                            String description, long fileSize) {

        public ModSource modSource() {
            if (url == null) return ModSource.SERVER;
            try {
                URI uri = new URI(url);
                if ("https".equalsIgnoreCase(uri.getScheme()) &&
                        "cdn.modrinth.com".equalsIgnoreCase(uri.getHost())) {
                    return ModSource.MODRINTH;
                }
            } catch (URISyntaxException ignored) {
            }
            return ModSource.UNTRUSTED;
        }

        public enum ModSource {
            SERVER,
            MODRINTH,
            CURSEFORGE,
            UNTRUSTED
        }
    }
}
