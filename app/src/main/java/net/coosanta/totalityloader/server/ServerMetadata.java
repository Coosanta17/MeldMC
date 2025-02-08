package net.coosanta.totalityloader.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ServerMetadata(
        String description,
        Optional<ServerMetadata.Players> players,
        Optional<ServerMetadata.Version> version,
        Optional<ServerMetadata.Favicon> favicon,
        boolean secureChatEnforced
) {
    public static final Codec<ServerMetadata> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.STRING.lenientOptionalFieldOf("description", "").forGetter(ServerMetadata::description),
                            ServerMetadata.Players.CODEC.lenientOptionalFieldOf("players").forGetter(ServerMetadata::players),
                            ServerMetadata.Version.CODEC.lenientOptionalFieldOf("version").forGetter(ServerMetadata::version),
                            ServerMetadata.Favicon.CODEC.lenientOptionalFieldOf("favicon").forGetter(ServerMetadata::favicon),
                            Codec.BOOL.lenientOptionalFieldOf("enforcesSecureChat", false).forGetter(ServerMetadata::secureChatEnforced)
                    )
                    .apply(instance, ServerMetadata::new)
    );

    public static record Favicon(byte[] iconBytes) {
        private static final String DATA_URI_PREFIX = "data:image/png;base64,";

        public static final Codec<ServerMetadata.Favicon> CODEC = Codec.STRING.comapFlatMap(uri -> {
            if (!uri.startsWith(DATA_URI_PREFIX)) {
                return DataResult.error(() -> "Unknown format");
            } else {
                try {
                    String base64Data = uri.substring(DATA_URI_PREFIX.length()).replaceAll("\n", "");
                    byte[] decoded = Base64.getDecoder().decode(base64Data.getBytes(StandardCharsets.UTF_8));
                    return DataResult.success(new ServerMetadata.Favicon(decoded));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Malformed base64 server icon");
                }
            }
        }, icon -> DATA_URI_PREFIX + new String(Base64.getEncoder().encode(icon.iconBytes), StandardCharsets.UTF_8));
    }

    public static record Players(int max, int online, List<ServerMetadata.SimpleGameProfile> sample) {
        private static final Codec<ServerMetadata.SimpleGameProfile> GAME_PROFILE_CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("id").forGetter(SimpleGameProfile::id),
                                Codec.STRING.fieldOf("name").forGetter(SimpleGameProfile::name)
                        )
                        .apply(instance, ServerMetadata.SimpleGameProfile::new)
        );

        public static final Codec<ServerMetadata.Players> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Codec.INT.fieldOf("max").forGetter(ServerMetadata.Players::max),
                                Codec.INT.fieldOf("online").forGetter(ServerMetadata.Players::online),
                                GAME_PROFILE_CODEC.listOf().lenientOptionalFieldOf("sample", List.of()).forGetter(ServerMetadata.Players::sample)
                        )
                        .apply(instance, ServerMetadata.Players::new)
        );
    }

    public static record Version(String gameVersion, int protocolVersion) {
        public static final Codec<ServerMetadata.Version> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                Codec.STRING.fieldOf("name").forGetter(ServerMetadata.Version::gameVersion),
                                Codec.INT.fieldOf("protocol").forGetter(ServerMetadata.Version::protocolVersion)
                        )
                        .apply(instance, ServerMetadata.Version::new)
        );
    }

    public static record SimpleGameProfile(UUID id, String name) {}
}
