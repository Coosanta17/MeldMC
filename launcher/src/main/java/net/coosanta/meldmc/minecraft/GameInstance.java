package net.coosanta.meldmc.minecraft;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.network.ProgressCallback;
import net.coosanta.meldmc.network.client.MeldData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GameInstance {
    private static final Logger log = LoggerFactory.getLogger(GameInstance.class);
    private final String ip;
    private @Nullable MeldData cachedMeldData;
    private @Nullable MeldData meldData;
    private final Path instanceDir;
    private final Path meldJson;

    public GameInstance(String ip) {
        this.ip = ip;

        // Handle illegal characters
        String dirName = ip.replaceAll("[:\\\\/*?\"<>|]", "_");
        Path gameDir = Main.getGameDir();
        this.instanceDir = gameDir.resolve(dirName);

        this.meldJson = instanceDir.resolve("meld.json");

        if (Files.exists(meldJson)) {
            this.cachedMeldData = readInstanceData();
        }
    }

    public void setMeldData(MeldData meldData) {
        this.meldData = meldData;
        createInstanceDirectory();
        if (cachedMeldData == null) {
            saveInstanceData();
        } else if (!cachedMeldData.equals(meldData)) {
            log.info("To be implemented...");
        }
    }

    private void createInstanceDirectory() {
        try {
            Files.createDirectories(instanceDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance directory for server: " + ip, e);
        }
    }

    private void saveInstanceData() {
        if (meldData == null) return;

        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(meldJson.toFile(), meldData);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save MeldData for server: " + ip, e);
        }
    }

    private @Nullable MeldData readInstanceData() {
        if (!Files.exists(meldJson)) return null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(meldJson.toFile(), MeldData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read MeldData for server: " + ip, e);
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

    public @Nullable MeldData getMeldData() {
        return meldData;
    }

    public @Nullable MeldData getCachedMeldData() {
        return cachedMeldData;
    }

    public String getIp() {
        return ip;
    }

    public boolean isMeldCached() {
        return cachedMeldData != null;
    }

    public Path getInstanceDir() {
        return instanceDir;
    }
}
