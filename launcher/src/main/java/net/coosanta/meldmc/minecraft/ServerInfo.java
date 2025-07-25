package net.coosanta.meldmc.minecraft;

import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.network.data.MeldServerStatusInfo;
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

/**
 * Represents information about a Minecraft server.
 * This class stores server details such as name, address, status information,
 * and provides functionality for serialization to NBT.
 */
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
    private boolean meldSupported;
    private String meldAddress;
    private int meldPort;
    private boolean isHttps;
    private boolean selfSigned;
    private MeldData meldData;

    /**
     * Creates a new server info instance with the given name and address.
     *
     * @param name    The display name of the server
     * @param address The address of the server (hostname or IP)
     */
    public ServerInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    /**
     * Creates a server info instance from an NBT compound tag.
     *
     * @param root The NBT compound tag containing server info data
     */
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

    /**
     * Copy constructor to create a new server info instance from an existing one.
     *
     * @param serverInfo The server info to copy from
     */
    public ServerInfo(ServerInfo serverInfo) {
        this(serverInfo.name, serverInfo.address);
        this.favicon = serverInfo.favicon;
        this.ping = serverInfo.ping;
        this.players = serverInfo.players;
        this.versionInfo = serverInfo.versionInfo;
        this.status = serverInfo.status;
        this.description = serverInfo.description;
        this.meldSupported = serverInfo.meldSupported;
        this.meldPort = serverInfo.meldPort;
    }

    /**
     * Converts this server info into an NBT compound tag for serialization.
     *
     * @return The NBT compound tag containing server data
     */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        tag.putString("ip", this.address);
        if (this.favicon != null) tag.putString("icon", Base64.getEncoder().encodeToString(this.favicon));

        return tag;
    }

    /**
     * Updates this server info with data from a server status response.
     *
     * @param statusInfo The server status information to add
     */
    public synchronized void addStatusInfo(ServerStatusInfo statusInfo) {
        this.description = statusInfo.getDescription();
        this.favicon = statusInfo.getIconPng();
        this.players = statusInfo.getPlayerInfo();
        this.versionInfo = statusInfo.getVersionInfo();
        if (statusInfo instanceof MeldServerStatusInfo meldStatus) {
            this.meldSupported = true; // Meld support checked in MeldClientboundStatusResponsePacket
            this.meldAddress = meldStatus.getAddress();
            this.meldPort = meldStatus.getPort();
            this.isHttps = meldStatus.isHttps();
            this.selfSigned = meldStatus.isSelfSigned();
        }
    }

    public synchronized void setMeldData(MeldData meldData) {
        this.meldData = meldData;
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

    public boolean isMeldSupported() {
        return meldSupported;
    }

    public int getMeldPort() {
        return meldPort;
    }

    public String getMeldAddress() {
        return meldAddress;
    }

    public boolean isSelfSigned() {
        return selfSigned;
    }

    public boolean isHttps() {
        return isHttps;
    }

    public synchronized MeldData getMeldData() {
        return meldData;
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

    /**
     * Enum representing the different connection states of a server.
     */
    public enum Status {
        INITIAL,
        PINGING,
        UNREACHABLE,
        INCOMPATIBLE,
        SUCCESSFUL
    }
}
