package net.coosanta.meldmc.minecraft;

import net.coosanta.meldmc.Main;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe singleton manager for Minecraft server list.
 * Handles loading, saving, and modifying the servers.dat NBT file.
 */
public class ServerListManager {
    private static final Logger log = LoggerFactory.getLogger(ServerListManager.class);
    private static volatile ServerListManager instance;

    private final Path gameDir;
    private CompoundTag serversDat;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<ServerInfo> serverCache = new ArrayList<>();

    private ServerListManager() {
        gameDir = Main.getGameDir();
        loadServersDat();
    }

    /**
     * Gets the singleton instance of ServerListManager
     *
     * @return The ServerListManager instance
     */
    public static ServerListManager getInstance() {
        if (instance == null) {
            synchronized (ServerListManager.class) {
                if (instance == null) {
                    instance = new ServerListManager();
                }
            }
        }
        return instance;
    }

    /**
     * Loads the servers.dat file from disk
     */
    private void loadServersDat() {
        lock.writeLock().lock();
        try {
            File serversDatFile = gameDir.resolve("servers.dat").toFile();

            if (!serversDatFile.isFile()) {
                log.info("servers.dat not found, creating new file");
                initializeEmptyServersDat(serversDatFile);
            } else {
                loadServersDatFromFile(serversDatFile);
            }

            refreshServerCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Creates an empty servers.dat file
     *
     * @param serversDatFile The file to create
     */
    private void initializeEmptyServersDat(File serversDatFile) {
        try {
            serversDat = new CompoundTag();
            serversDat.put("servers", new ListTag<>(CompoundTag.class));
            NBTUtil.write(serversDat, serversDatFile, false);
        } catch (IOException e) {
            log.error("Failed to initialize empty servers.dat", e);
            throw new RuntimeException("Failed to initialize empty servers.dat", e);
        }
    }

    /**
     * Loads servers.dat from an existing file
     *
     * @param serversDatFile The file to load from
     */
    private void loadServersDatFromFile(File serversDatFile) {
        try {
            Tag<?> serversDatRaw = NBTUtil.read(serversDatFile).getTag();
            if (serversDatRaw.getID() != 10) {
                throw new IllegalArgumentException("Invalid tags in servers.dat - Expected Compound");
            }
            serversDat = (CompoundTag) serversDatRaw;
        } catch (IOException e) {
            log.error("Failed to load servers.dat", e);
            throw new RuntimeException("Failed to load servers.dat", e);
        }
    }

    /**
     * Extracts the servers list from the NBT data
     *
     * @return The list of server CompoundTags
     */
    private ListTag<CompoundTag> extractServersList() {
        Tag<?> serversListRaw = serversDat.get("servers");
        if (serversListRaw == null) {
            log.warn("Missing 'servers' tag in servers.dat, creating empty list");
            ListTag<CompoundTag> emptyList = new ListTag<>(CompoundTag.class);
            serversDat.put("servers", emptyList);
            return emptyList;
        }

        if (serversListRaw.getID() != 9) {
            throw new IllegalArgumentException("Invalid tags in servers.dat root compound tag - Expected List");
        }

        ListTag<?> serversListUnchecked = (ListTag<?>) serversListRaw;
        if (serversListUnchecked.getTypeClass() != CompoundTag.class) {
            throw new IllegalArgumentException("Invalid tags in servers.dat List - Expected Compound");
        }

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> serversList = (ListTag<CompoundTag>) serversListUnchecked;
        return serversList;
    }

    /**
     * Updates the in-memory cache of servers from the NBT data
     */
    private void refreshServerCache() {
        lock.writeLock().lock();
        try {
            ListTag<CompoundTag> serversList = extractServersList();
            serverCache.clear();

            serversList.forEach(server -> serverCache.add(new ServerInfo(server)));
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the list of servers
     *
     * @return An unmodifiable list of ServerInfo objects
     */
    public List<ServerInfo> getServers() {
        lock.readLock().lock();
        try {
            return List.copyOf(serverCache);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a new server to the list
     *
     * @param server The ServerInfo to add
     */
    public void addServer(ServerInfo server) {
        lock.writeLock().lock();
        try {
            ListTag<CompoundTag> serversList = extractServersList();
            serversList.add(server.toNbt());
            saveServersDat();
            refreshServerCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Updates an existing server in the list
     *
     * @param index The index of the server to update
     * @param updatedServer The updated ServerInfo
     * @throws IndexOutOfBoundsException If the index is out of range
     */
    public void updateServer(int index, ServerInfo updatedServer) {
        lock.writeLock().lock();
        try {
            ListTag<CompoundTag> serversList = extractServersList();
            if (index < 0 || index >= serversList.size()) {
                throw new IndexOutOfBoundsException("Server index out of range: " + index);
            }

            serversList.set(index, updatedServer.toNbt());
            saveServersDat();
            refreshServerCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a server from the list
     *
     * @param index The index of the server to remove
     * @throws IndexOutOfBoundsException If the index is out of range
     */
    public void removeServer(int index) {
        lock.writeLock().lock();
        try {
            ListTag<CompoundTag> serversList = extractServersList();
            if (index < 0 || index >= serversList.size()) {
                throw new IndexOutOfBoundsException("Server index out of range: " + index);
            }

            serversList.remove(index);
            saveServersDat();
            refreshServerCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Moves a server to a new position in the list
     *
     * @param fromIndex The current index of the server
     * @param toIndex The desired index for the server
     * @throws IndexOutOfBoundsException If either index is out of range
     */
    public void moveServer(int fromIndex, int toIndex) {
        lock.writeLock().lock();
        try {
            ListTag<CompoundTag> serversList = extractServersList();
            if (fromIndex < 0 || fromIndex >= serversList.size() ||
                toIndex < 0 || toIndex >= serversList.size()) {
                throw new IndexOutOfBoundsException("Server index out of range");
            }

            CompoundTag serverTag = serversList.remove(fromIndex);
            serversList.add(toIndex, serverTag);
            saveServersDat();
            refreshServerCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Saves the servers.dat file to disk
     */
    public void saveServersDat() {
        lock.writeLock().lock();
        try {
            File serversDatFile = gameDir.resolve("servers.dat").toFile();
            NBTUtil.write(serversDat, serversDatFile, false);
        } catch (IOException e) {
            log.error("Failed to save servers.dat", e);
            throw new RuntimeException("Failed to save servers.dat", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Reloads the server list from disk
     */
    public void reloadServerList() {
        loadServersDat();
    }
}
