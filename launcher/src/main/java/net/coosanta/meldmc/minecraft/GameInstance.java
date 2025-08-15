package net.coosanta.meldmc.minecraft;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.network.ProgressCallback;
import net.coosanta.meldmc.network.client.MeldClient;
import net.coosanta.meldmc.network.client.MeldClientRegistry;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GameInstance {
    private static final Logger log = LoggerFactory.getLogger(GameInstance.class);

    private final String address;
    private @Nullable MeldData cachedMeldData;
    private @Nullable MeldData meldData;
    private final Path instanceDir;
    private final Path meldJson;

    private final Map<String, MeldData.ClientMod> changedMods = new HashMap<>();
    boolean modLoaderChanged;
    boolean modLoaderVersionChanged;
    boolean mcVersionChanged;

    public GameInstance(String address) {
        this.address = address;

        // Handle illegal characters
        String dirName = "meldinstances/" + address.replaceAll("[:\\\\/*?\"<>|]", "_");
        this.instanceDir = Main.getGameDir().resolve(dirName);

        this.meldJson = instanceDir.resolve("meld.json");

        if (Files.exists(meldJson)) {
            this.cachedMeldData = readInstanceData();
        }
    }

    public void setMeldData(MeldData meldData) {
        this.meldData = meldData;
        createInstanceDirectory();
        if (!Objects.equals(cachedMeldData, meldData)) {
            boolean noCachedData = cachedMeldData == null;
            modLoaderChanged = noCachedData || cachedMeldData.modLoader() != meldData.modLoader();
            modLoaderVersionChanged = noCachedData || !cachedMeldData.modLoaderVersion().equals(meldData.modLoaderVersion());
            mcVersionChanged = noCachedData || !cachedMeldData.mcVersion().equals(meldData.mcVersion());

            changedMods.clear();

            var oldMods = noCachedData ? Collections.<String, MeldData.ClientMod>emptyMap() : cachedMeldData.modMap();
            meldData.modMap().forEach((key, value) -> {
                if (!value.equals(oldMods.get(key))) {
                    changedMods.put(key, value);
                }
            });
            saveInstanceData();
        }
    }

    private void createInstanceDirectory() {
        try {
            Files.createDirectories(instanceDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance directory for server: " + address, e);
        }
    }

    private void saveInstanceData() {
        if (meldData == null) return;

        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(meldJson.toFile(), meldData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save MeldData for server: " + address, e);
        }
    }

    private @Nullable MeldData readInstanceData() {
        if (!Files.exists(meldJson)) return null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(meldJson.toFile(), MeldData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read MeldData for server: " + address, e);
        }
    }

    public CompletableFuture<Path> backupInstanceDirectory(@Nullable ProgressCallback progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path baseBackupDir = Main.getGameDir().resolve("backups/meld_instances");
                Files.createDirectories(baseBackupDir);

                String baseFileName = instanceDir.getFileName().toString();
                Path zipPath = baseBackupDir.resolve(baseFileName + ".zip");

                // Appending number if duplicate.
                int counter = 1;
                while (Files.exists(zipPath)) {
                    zipPath = baseBackupDir.resolve(baseFileName + "_" + counter + ".zip");
                    counter++;
                }

                long totalFiles = 0;
                if (progressCallback != null) {
                    try (var walk = Files.walk(instanceDir)) {
                        totalFiles = walk.filter(path -> !Files.isDirectory(path)).count();
                    }
                }

                final long finalTotalFiles = totalFiles;
                final long[] filesProcessed = {0};

                try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath));
                     var walk = Files.walk(instanceDir)) {
                    walk.filter(path -> !Files.isDirectory(path)).forEach(path -> {
                        try {
                            String relativePath = instanceDir.relativize(path).toString().replace("\\", "/");

                            if (progressCallback != null) {
                                progressCallback.onProgress(filesProcessed[0], finalTotalFiles, relativePath);
                            }

                            zos.putNextEntry(new ZipEntry(relativePath));
                            Files.copy(path, zos);
                            zos.closeEntry();

                            if (progressCallback != null) {
                                filesProcessed[0]++;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to zip: " + path, e);
                        }
                    });
                }

                return zipPath;
            } catch (IOException e) {
                throw new RuntimeException("Backup failed", e);
            }
        });
    }

    public CompletableFuture<@Nullable Path> deleteInstance(boolean createBackup, @Nullable ProgressCallback progressCallback) {
        if (!Files.exists(instanceDir)) return null;

        if (createBackup) {
            return backupInstanceDirectory(progressCallback).thenApply(backupPath -> {
                try {
                    deleteInstanceFiles();
                    return backupPath;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete instance after backup", e);
                }
            });
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    deleteInstanceFiles();
                    return null;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete instance", e);
                }
            });
        }
    }

    private void deleteInstanceFiles() throws IOException {
        try (var paths = Files.walk(instanceDir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to delete: " + path, e);
                        }
                    });
        }
    }

    public void downloadMods(ProgressCallback progressCallback) {
        if (meldData == null) throw new NullPointerException("MeldData cannot be null");

        Map<String, MeldData.ClientMod> serverMods = new HashMap<>();
        Map<String, MeldData.ClientMod> webMods = new HashMap<>();

        MeldClient meldClient = MeldClientRegistry.getClient(address);
        meldClient.setProgressCallback(progressCallback);
        var serverModsDownloadFuture = meldClient.downloadFiles(serverMods.keySet(), instanceDir.resolve("mods"));
        serverModsDownloadFuture.thenAccept(paths -> paths.forEach(path -> {
            try {
                if (!changedMods.containsKey(ResourceUtil.calculateSHA512(path.toFile()))) {
                    // TODO Unequal Hashes - !IMPORTANT! handling of manipulated data
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public @Nullable MeldData getMeldData() {
        return meldData;
    }

    public @Nullable MeldData getCachedMeldData() {
        return cachedMeldData;
    }

    public String getAddress() {
        return address;
    }

    public boolean isMeldCached() {
        return cachedMeldData != null;
    }

    public Path getInstanceDir() {
        return instanceDir;
    }

    public Map<String, MeldData.ClientMod> getChangedMods() {
        return changedMods;
    }
}
