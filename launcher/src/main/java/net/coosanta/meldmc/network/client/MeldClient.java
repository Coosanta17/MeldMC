package net.coosanta.meldmc.network.client;

import net.coosanta.meldmc.network.ProgressCallback;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Client interface for interacting with a MeldMC server.
 */
public interface MeldClient {

    /**
     * Fetches mod information from the server.
     *
     * @return A CompletableFuture that resolves to the mod information
     */
    CompletableFuture<MeldData> fetchModInfo();

    /**
     * Downloads files with the specified hashes.
     *
     * @param hashes      Collection of file hashes to download
     * @param destination Directory to save the downloaded files
     * @return A CompletableFuture that resolves to a list of downloaded file paths
     */
    CompletableFuture<Set<Path>> downloadFiles(Collection<String> hashes, Path destination);

    /**
     * Sets a callback for progress updates during downloads.
     *
     * @param callback The progress callback
     */
    void setProgressCallback(ProgressCallback callback);

    /**
     * Closes the client and releases resources.
     */
    void close();
}
