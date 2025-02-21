package net.coosanta.totalityloader.minecraft;

import net.kyori.adventure.text.Component;
import net.querz.nbt.tag.CompoundTag;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Base64;

public class ServerInfo {
    private static final Logger log = LoggerFactory.getLogger(ServerInfo.class);

    private String name;
    private String address;

    private Component description;
    private @Nullable byte[] favicon;
    private long ping;
    private @Nullable PlayerInfo players;
    private VersionInfo versionInfo;
    private ServerInfo.Status status = Status.INITIAL;

    public ServerInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public ServerInfo(CompoundTag root) {
        this(root.getString("name"), root.getString("ip"));
        if (root.containsKey("icon")) {
            try {
                setFavicon(Base64.getDecoder().decode(root.getString("icon")));
            } catch (IllegalArgumentException e) {
                log.error("Malformed base64 server icon", e);
            }
        }
    }

    public ServerInfo(ServerInfo serverInfo) {
        this(serverInfo.name, serverInfo.address);
        this.favicon = serverInfo.favicon;
        this.ping = serverInfo.ping;
        this.players = serverInfo.players;
        this.versionInfo = serverInfo.versionInfo;
        this.status = serverInfo.status;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        tag.putString("ip", this.address);
        if (this.favicon != null) tag.putString("icon", Base64.getEncoder().encodeToString(this.favicon));

        return tag;
    }

    public void addStatusInfo(ServerStatusInfo statusInfo) {
        this.description = statusInfo.getDescription();
        this.favicon = statusInfo.getIconPng();
        this.players = statusInfo.getPlayerInfo();
        this.versionInfo = statusInfo.getVersionInfo();
    }

    public void setFavicon(@Nullable byte[] favicon) {
        this.favicon = favicon;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    @Nullable
    public byte[] getFavicon() {
        return favicon;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public @Nullable PlayerInfo getPlayers() {
        return players;
    }

    public Status getStatus() {
        return status;
    }

    public Component getDescription() {
        return description;
    }

    public long getPing() {
        return ping;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", description=" + description +
                ", favicon=" + Arrays.toString(favicon) +
                ", ping=" + ping +
                ", players=" + players +
                ", versionInfo=" + versionInfo +
                ", status=" + status +
                '}';
    }

    public enum Status {
        INITIAL,
        PINGING,
        UNREACHABLE,
        INCOMPATIBLE,
        SUCCESSFUL
    }
}
