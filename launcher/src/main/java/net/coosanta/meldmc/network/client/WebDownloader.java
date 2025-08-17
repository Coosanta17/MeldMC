package net.coosanta.meldmc.network.client;

import net.coosanta.meldmc.network.ProgressCallback;
import net.coosanta.meldmc.network.ProgressTrackingInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WebDownloader implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(WebDownloader.class);
    private final HttpClient client;
    private final ExecutorService downloadExecutor;
    private final ExecutorService coordinationExecutor;

    private volatile ProgressCallback totalProgressCallback;
    private volatile long totalExpectedBytes;

    private volatile ProgressCallback fileProgressCallback;
    private volatile long totalExpectedFiles;

    public WebDownloader() {
        coordinationExecutor = Executors.newSingleThreadExecutor(r -> {
            var t = new Thread(r, "WebDownloader-Coordinator");
            t.setDaemon(true);
            return t;
        });
        downloadExecutor = Executors.newFixedThreadPool(
                3 * Runtime.getRuntime().availableProcessors(),
                r -> {
                    var t = new Thread(r, "WebDownloader-Worker");
                    t.setDaemon(true);
                    return t;
                }
        );

        this.client = HttpClient.newBuilder()
                .executor(downloadExecutor)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<Set<Path>> downloadMods(Collection<MeldData.ClientMod> mods, Path destinationDir) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Files.createDirectories(destinationDir);

                totalExpectedBytes = mods.stream()
                        .filter(mod -> mod.url() != null)
                        .mapToLong(MeldData.ClientMod::fileSize)
                        .sum();

                totalExpectedFiles = mods.stream()
                        .filter(mod -> mod.url() != null)
                        .count();

                Set<CompletableFuture<Path>> futures = mods.stream()
                        .filter(mod -> mod.url() != null)
                        .map(mod -> downloadMod(mod, destinationDir)
                                .exceptionally(ex -> {
                                    log.error("Failed to download {}: ", mod.filename(), ex);
                                    // Ignoring failed mods, because it will either be sent by server or user error
                                    // TODO: Handle IOException
                                    return null;
                                }))
                        .collect(Collectors.toSet());

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet()));
            } catch (IOException e) {
                return CompletableFuture.<Set<Path>>failedFuture(
                        new RuntimeException("Failed to create destination directory", e)
                );
            }
        }, coordinationExecutor).thenCompose(future -> future);
    }

    private CompletableFuture<Path> downloadMod(MeldData.ClientMod mod, Path destinationDir) {
        Path targetPath = destinationDir.resolve(mod.filename());

        if (mod.modSource() == MeldData.ClientMod.ModSource.SERVER) {
            return CompletableFuture.failedFuture(
                    new UnsupportedOperationException("Cannot download server-sent mods via url for mod: " + mod.filename())
            );
        }
        if (mod.url() == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Null URL at mod: " + mod.filename())
            );
        }

        // TODO; Potentially skip files if hash is the same?

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mod.url()))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP error " + response.statusCode() + " for " + mod.filename());
                    }

                    try (ProgressTrackingInputStream progressStream = new ProgressTrackingInputStream(
                            response.body(),
                            mod.fileSize(),
                            false,
                            (deltaBytesRead, aaaaaaa, unused) -> {
                                if (totalProgressCallback == null) return;
                                if (deltaBytesRead > 0) {
                                    totalProgressCallback.onProgress(deltaBytesRead, totalExpectedBytes);
                                }
                            }
                    )) {
                        Files.copy(progressStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

                        if (fileProgressCallback != null) {
                            fileProgressCallback.onProgress(1, totalExpectedFiles);
                        }

                        return targetPath;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to save " + mod.filename(), e); // TODO
                    }
                });
    }

    public void setTotalProgressCallback(ProgressCallback callback) {
        this.totalProgressCallback = callback;
    }

    public void setFileProgressCallback(ProgressCallback callback) {
        this.fileProgressCallback = callback;
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        coordinationExecutor.shutdown();
        downloadExecutor.shutdown();
    }
}
