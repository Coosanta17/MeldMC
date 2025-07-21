package net.coosanta.meldmc.network.client;

import java.nio.file.Path;
import java.util.List;
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
     * @param hashes List of file hashes to download
     * @param destination Directory to save the downloaded files
     * @return A CompletableFuture that resolves to a list of downloaded file paths
     */
    CompletableFuture<List<Path>> downloadFiles(List<String> hashes, Path destination);

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

    /**
     * Interface for progress updates.
     */
    interface ProgressCallback {
        /**
         * Called with progress updates.
         *
         * @param bytesTransferred Number of bytes transferred so far
         * @param totalBytes Total number of bytes to transfer, or -1 if unknown
         * @param fileName Name of the current file, or null if not applicable
         */
        void onProgress(long bytesTransferred, long totalBytes, String fileName);
    }
}
