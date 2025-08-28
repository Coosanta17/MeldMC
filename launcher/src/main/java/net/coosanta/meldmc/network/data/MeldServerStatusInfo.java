package net.coosanta.meldmc.network.data;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.jetbrains.annotations.Nullable;

public class MeldServerStatusInfo extends ServerStatusInfo {
    private final String address;
    private final boolean isHttps;
    private final boolean selfSigned;

    public MeldServerStatusInfo(Component description, @Nullable PlayerInfo playerInfo,
                                @Nullable VersionInfo versionInfo, byte[] iconPng, boolean enforcesSecureChat,
                                String address, boolean isHttps, boolean selfSigned) {

        super(description, playerInfo, versionInfo, iconPng, enforcesSecureChat);
        this.address = address;
        this.isHttps = isHttps;
        this.selfSigned = selfSigned;
    }

    public String getAddress() {
        return address;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public boolean isSelfSigned() {
        return selfSigned;
    }
}
