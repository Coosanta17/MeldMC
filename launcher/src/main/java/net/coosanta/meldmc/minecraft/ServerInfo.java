package net.coosanta.meldmc.minecraft;

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
    /** Logger for this class */
    private static final Logger log = LoggerFactory.getLogger(ServerInfo.class);

    /** The display name of the server */
    private String name;
    /** The server address (hostname or IP) */
    private String address;

    /** The server description/MOTD */
    private Component description;
    /** The server favicon as a byte array (may be null) */
    private @Nullable byte[] favicon;
    /** The ping time in milliseconds */
    private long ping;
    /** Information about players on the server (may be null) */
    private @Nullable PlayerInfo players;
    /** Information about the server version */
    private VersionInfo versionInfo;
    /** The current status of the server connection */
    private ServerInfo.Status status = Status.INITIAL;

    /**
     * Creates a new server info instance with the given name and address.
     *
     * @param name The display name of the server
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
    public void addStatusInfo(ServerStatusInfo statusInfo) {
        this.description = statusInfo.getDescription();
        this.favicon = statusInfo.getIconPng();
        this.players = statusInfo.getPlayerInfo();
        this.versionInfo = statusInfo.getVersionInfo();
    }

    /**
     * Sets the server favicon.
     *
     * @param favicon The favicon byte array, or null if none
     */
    public void setFavicon(@Nullable byte[] favicon) {
        this.favicon = favicon;
    }

    /**
     * Updates the status of this server.
     *
     * @param status The new server status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Sets the display name of the server.
     *
     * @param name The new server name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the server address.
     *
     * @param address The new server address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets the ping time for this server.
     *
     * @param ping The ping time in milliseconds
     */
    public void setPing(long ping) {
        this.ping = ping;
    }

    /**
     * Gets the display name of this server.
     *
     * @return The server name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the address of this server.
     *
     * @return The server address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the server favicon.
     *
     * @return The favicon as a byte array, or null if none exists
     */
    @Nullable
    public byte[] getFavicon() {
        return favicon;
    }

    /**
     * Gets information about the server version.
     *
     * @return The server version info
     */
    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    /**
     * Gets information about players on the server.
     *
     * @return The player information, or null if not available
     */
    public @Nullable PlayerInfo getPlayers() {
        return players;
    }

    /**
     * Gets the current connection status of this server.
     *
     * @return The server status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Gets the server description/MOTD.
     *
     * @return The server description component
     */
    public Component getDescription() {
        return description;
    }

    /**
     * Gets the ping time for this server.
     *
     * @return The ping time in milliseconds
     */
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

    /**
     * Enum representing the different connection states of a server.
     */
    public enum Status {
        /** Initial state before any connection attempts */
        INITIAL,
        /** Currently attempting to ping the server */
        PINGING,
        /** Server could not be reached */
        UNREACHABLE,
        /** Server was reached but is incompatible */
        INCOMPATIBLE,
        /** Successfully connected to the server */
        SUCCESSFUL
    }
}
