package net.coosanta.meldmc.minecraft;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.minecraft.launcher.ClientLauncher;
import net.coosanta.meldmc.minecraft.launcher.LaunchArgs;
import net.coosanta.meldmc.network.ProgressCallback;
import net.coosanta.meldmc.network.UnifiedProgressTracker;
import net.coosanta.meldmc.network.client.MeldClientRegistry;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.network.client.WebModsDownloader;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GameInstance {
    private static final Logger log = LoggerFactory.getLogger(GameInstance.class);

    private final String address;
    private @Nullable MeldData cachedMeldData;
    private @Nullable MeldData meldData;
    private final Path instanceDir;
    private final Path meldJson;
    private final Path modsDir;

    private final Map<String, MeldData.ClientMod> changedMods = new HashMap<>();
    private final Map<String, Path> deletedMods = new HashMap<>();
    boolean modLoaderChanged;
    boolean modLoaderVersionChanged;
    boolean mcVersionChanged;

    public GameInstance(String address) {
        this.address = address;

        // Handle illegal characters
        String dirName = "meldinstances/" + address.replaceAll("[:\\\\/*?\"<>|]", "_");
        this.instanceDir = Main.getLaunchArgs().getGameDir().resolve(dirName);

        this.meldJson = instanceDir.resolve("meld.json");

        this.modsDir = getInstanceDir().resolve("mods");

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
        }

        changedMods.clear();
        deletedMods.clear();

        var newModsByName = meldData.modMap().values().stream()
                .collect(Collectors.toMap(MeldData.ClientMod::filename, m -> m));

        // scan files for tampering or extras TODO: Non-blocking, with slow disk handling
        if (Files.exists(modsDir)) {
            try (var stream = Files.list(modsDir).filter(Files::isRegularFile)) {
                stream.forEach(path -> checkChanges(path, newModsByName));
            } catch (IOException e) {
                throw new RuntimeException("Failed to scan mods directory", e);
            }
        }

        // Add any remaining entries that are missing on disk - all other mods have been removed when checking for changes
        newModsByName.values()
                .forEach(mod -> changedMods.put(mod.hash(), mod));

        log.debug("Detected {} new or changed mods", changedMods.size());

        saveInstanceData();

    }

    private void checkChanges(Path path, Map<String, MeldData.@NotNull ClientMod> newModsByName) {
        String name = path.getFileName().toString();
        var expected = newModsByName.remove(name);
        if (expected != null) {
            // Check for change in data and add to changedMods accordingly.
            try {
                String currentHash = ResourceUtil.calculateSHA512(path.toFile());
                if (!currentHash.equals(expected.hash())) {
                    changedMods.put(expected.hash(), expected);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to hash mod file: " + name, e);
            }
        } else {
            // rename file if hash matches, else delete.
            try {
                String currentHash = ResourceUtil.calculateSHA512(path.toFile());
                var matchEntry = newModsByName.entrySet().stream()
                        .filter(e -> e.getValue().hash().equals(currentHash))
                        .findFirst();
                if (matchEntry.isPresent()) {
                    String correctName = matchEntry.get().getValue().filename();
                    Files.move(path, modsDir.resolve(correctName));
                    newModsByName.remove(matchEntry.get().getKey());
                } else {
                    deletedMods.put(name, path);
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to process mod file: " + name, e);
            }
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
                Path baseBackupDir = Main.getLaunchArgs().getGameDir().resolve("backups/meld_instances");
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

    public void downloadModsAndLaunch(UnifiedProgressTracker progressTracker, LaunchArgs launchArgs) {
        // Skip mod downloading if there are no changed mods
        if (changedMods.isEmpty()) {
            log.info("No mods to download, proceeding to launch");
            removeDeletedMods();
            progressTracker.completeAllProgress();
            Platform.runLater(() -> launchGame(launchArgs, progressTracker));
            return;
        }

        var webMods = getChangedMods().values().stream()
                .filter(mod -> mod.modSource() != MeldData.ClientMod.ModSource.SERVER)
                .collect(Collectors.toSet());

        var serverMods = getChangedMods().values().stream()
                .filter(mod -> mod.modSource() == MeldData.ClientMod.ModSource.SERVER)
                .collect(Collectors.toSet());

        long totalBytes = webMods.stream().mapToLong(MeldData.ClientMod::fileSize).sum() +
                          serverMods.stream().mapToLong(MeldData.ClientMod::fileSize).sum();
        long totalFiles = webMods.size() + serverMods.size();

        progressTracker.setTotalExpected(totalBytes, totalFiles);

        removeDeletedMods();

        @SuppressWarnings("resource") // closed in whenComplete at end of method.
        var webDownloader = new WebModsDownloader();

        webDownloader.setTotalProgressCallback((deltaBytes, total, unused) ->
                progressTracker.addBytesProgress(deltaBytes));

        webDownloader.setFileProgressCallback((deltaDownloadedFiles, total, unused) ->
                progressTracker.addFileProgress(deltaDownloadedFiles));

        progressTracker.setStage(UnifiedProgressTracker.LaunchStage.MODS);

        // Web downloads
        CompletableFuture<Set<Path>> webDownloadFuture = webMods.isEmpty()
                ? CompletableFuture.completedFuture(Set.of())
                : webDownloader.downloadMods(webMods, modsDir);

        // Server downloads
        CompletableFuture<Set<Path>> serverDownloadFuture = CompletableFuture.completedFuture(Set.of());
        if (!serverMods.isEmpty()) {
            var meldClient = MeldClientRegistry.getClient(getAddress());
            if (meldClient != null) {
                meldClient.setProgressCallback((newBytes, total, filename) ->
                        progressTracker.addBytesProgress(newBytes));

                var serverHashes = serverMods.stream()
                        .map(MeldData.ClientMod::hash)
                        .collect(Collectors.toSet());

                serverDownloadFuture = meldClient.downloadFiles(serverHashes, modsDir)
                        .whenComplete((paths, ex) -> {
                            if (paths != null) progressTracker.addFileProgress(serverMods.size());
                        });
            }
        }

        // Wait for both downloads to complete
        CompletableFuture<Set<Path>> finalServerDownloadFuture = serverDownloadFuture;
        CompletableFuture.allOf(webDownloadFuture, serverDownloadFuture)
                .thenAccept(v -> {
                    try {
                        Set<Path> webPaths = webDownloadFuture.join();
                        Set<Path> serverPaths = finalServerDownloadFuture.join();

                        log.debug("Downloaded {} web mods.", webPaths.size());
                        log.debug("Downloaded {} server mods.", serverPaths.size());

                        progressTracker.completeAllProgress();

                        Set<Path> allDownloaded = Stream
                                .concat(webPaths.stream(), serverPaths.stream())
                                .collect(Collectors.toSet());

                        for (Path path : allDownloaded) {
                            try {
                                String sha = ResourceUtil.calculateSHA512(path.toFile());
                                if (!changedMods.containsKey(sha)) {
                                    log.error("Downloaded hashes do not match expected value!");
                                    // TODO Unequal Hashes - handling of manipulated data
                                }
                            } catch (IOException | NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        Platform.runLater(() -> launchGame(launchArgs, progressTracker));
                    } catch (Exception e) {
                        log.error("Error retrieving download results", e);
                        Platform.runLater(() -> {
                            // TODO: Error
                        });
                    }
                })
                .exceptionally(ex -> {
                    log.error("Mod download failed", ex);
                    Platform.runLater(() -> {
                        // TODO: Error
                    });
                    return null;
                })
                .whenComplete((v, ex) -> webDownloader.close());
    }

    private void launchGame(LaunchArgs launchArgs, UnifiedProgressTracker progressTracker) {
        try {
            if (meldData == null) {
                log.error("Cannot launch game: MeldData is null");
                return;
            }

            log.info("Launching Minecraft for instance: {}", address);

            var launcher = new ClientLauncher(progressTracker);
            Process process = launcher.launch(this, launchArgs);

            log.info("Game launched successfully (PID: {})", process.pid());

            process.onExit().thenRun(() -> {
                Platform.exit();
                System.exit(0);
            });

            Platform.exit();
        } catch (Exception e) {
            log.error("Failed to launch game", e);
            // TODO: Show error in GUI
        }
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

    public Map<String, Path> getDeletedMods() {
        return deletedMods;
    }

    public void removeDeletedMods() {
        for (Path file : deletedMods.values()) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                log.warn("Failed to delete mod file: {}", file, e);
            }
        }
        deletedMods.clear();
    }
}
