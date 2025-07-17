package net.coosanta.meldmc.network.data;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.jetbrains.annotations.Nullable;

public class MeldServerStatusInfo extends ServerStatusInfo {
    private final boolean meldSupported;
    private final int port;

    public MeldServerStatusInfo(Component description, @Nullable PlayerInfo playerInfo,
                                @Nullable VersionInfo versionInfo, byte[] iconPng,
                                boolean enforcesSecureChat, boolean meldSupported, int port) {
        super(description, playerInfo, versionInfo, iconPng, enforcesSecureChat);
        this.meldSupported = meldSupported;
        this.port = port;
    }

    public boolean isMeldSupported() {
        return meldSupported;
    }

    public int getPort() {
        return port;
    }

    // Potentially unnecessary
    public static MeldServerStatusInfo from(ServerStatusInfo original, boolean meldSupported, int port) {
        return new MeldServerStatusInfo(
                original.getDescription(),
                original.getPlayerInfo(),
                original.getVersionInfo(),
                original.getIconPng(),
                original.isEnforcesSecureChat(),
                meldSupported,
                port
        );
    }
}
