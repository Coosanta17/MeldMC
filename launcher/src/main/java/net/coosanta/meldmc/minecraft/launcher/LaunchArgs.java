package net.coosanta.meldmc.minecraft.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static net.coosanta.meldmc.Main.DESIGN_HEIGHT;
import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class LaunchArgs {
    private static final Logger log = LoggerFactory.getLogger(LaunchArgs.class);

    private String username;
    private String version;
    private Path gameDir;
    private Path assetsDir;
    private String assetIndex;
    private String uuid;
    private String accessToken;
    private String clientId;
    private String xuid;
    private String userType;
    private String versionType;
    private boolean demo;

    private Integer width = DESIGN_WIDTH;
    private Integer height = DESIGN_HEIGHT;

    // Unknown if these will work.
    private String quickPlayPath;
    private String quickPlaySingleplayer;
    private String quickPlayMultiplayer;
    private String quickPlayRealms;

    public static LaunchArgs parse(String[] args) {
        LaunchArgs la = new LaunchArgs();

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "--username" -> la.username = next(args, ++i, a);
                case "--version" -> la.version = next(args, ++i, a);
                case "--gameDir" -> la.gameDir = Path.of(next(args, ++i, a));
                case "--assetsDir" -> la.assetsDir = Path.of(next(args, ++i, a));
                case "--assetIndex" -> la.assetIndex = next(args, ++i, a);
                case "--uuid" -> la.uuid = next(args, ++i, a);
                case "--accessToken" -> la.accessToken = next(args, ++i, a);
                case "--clientId" -> la.clientId = next(args, ++i, a);
                case "--xuid" -> la.xuid = next(args, ++i, a);
                case "--userType" -> la.userType = next(args, ++i, a);
                case "--versionType" -> la.versionType = next(args, ++i, a);

                case "--demo" -> la.demo = true;

                case "--width" -> la.width = parseInt(next(args, ++i, a), "--width");
                case "--height" -> la.height = parseInt(next(args, ++i, a), "--height");

                case "--quickPlayPath" -> la.quickPlayPath = next(args, ++i, a);
                case "--quickPlaySingleplayer" -> la.quickPlaySingleplayer = next(args, ++i, a);
                case "--quickPlayMultiplayer" -> la.quickPlayMultiplayer = next(args, ++i, a);
                case "--quickPlayRealms" -> la.quickPlayRealms = next(args, ++i, a);

                default -> log.debug("Unknown arg ignored: {}", a);
            }
        }

        return la;
    }

    private static String next(String[] args, int idx, String key) {
        if (idx >= args.length) {
            throw new IllegalArgumentException("Missing value for " + key);
        }
        return args[idx];
    }

    private static Integer parseInt(String v, String key) {
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for " + key + ": " + v, e);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getVersion() {
        return version;
    }

    public Path getGameDir() {
        return gameDir;
    }

    public Path getAssetsDir() {
        return assetsDir;
    }

    public String getAssetIndex() {
        return assetIndex;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getXuid() {
        return xuid;
    }

    public String getUserType() {
        return userType;
    }

    public String getVersionType() {
        return versionType;
    }

    public boolean isDemo() {
        return demo;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getQuickPlayPath() {
        return quickPlayPath;
    }

    public String getQuickPlaySingleplayer() {
        return quickPlaySingleplayer;
    }

    public String getQuickPlayMultiplayer() {
        return quickPlayMultiplayer;
    }

    public String getQuickPlayRealms() {
        return quickPlayRealms;
    }
}
