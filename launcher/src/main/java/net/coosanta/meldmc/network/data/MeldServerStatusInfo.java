package net.coosanta.meldmc.network.data;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.jetbrains.annotations.Nullable;

public class MeldServerStatusInfo extends ServerStatusInfo {
    private final boolean meldSupported;

    public MeldServerStatusInfo(Component description, @Nullable PlayerInfo playerInfo,
                                @Nullable VersionInfo versionInfo, byte[] iconPng,
                                boolean enforcesSecureChat, boolean meldSupported) {
        super(description, playerInfo, versionInfo, iconPng, enforcesSecureChat);
        this.meldSupported = meldSupported;
    }

    public boolean isMeldSupported() {
        return meldSupported;
    }

    // Potentially unnecessary
    public static MeldServerStatusInfo from(ServerStatusInfo original, boolean meldSupported) {
        return new MeldServerStatusInfo(
                original.getDescription(),
                original.getPlayerInfo(),
                original.getVersionInfo(),
                original.getIconPng(),
                original.isEnforcesSecureChat(),
                meldSupported
        );
    }
}
