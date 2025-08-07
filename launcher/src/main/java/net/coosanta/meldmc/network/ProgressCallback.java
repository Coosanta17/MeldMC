package net.coosanta.meldmc.network;

/**
 * Interface for progress updates.
 */
@FunctionalInterface
public interface ProgressCallback {
    /**
     * Called to report progress updates.
     *
     * @param progress the current progress value
     * @param total    the total value to reach
     * @param context  optional additional context objects
     */
    void onProgress(long progress, long total, Object... context);
}
