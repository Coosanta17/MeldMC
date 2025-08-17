package net.coosanta.meldmc.network;

import java.util.concurrent.atomic.AtomicLong;

public class UnifiedProgressTracker {
    private final AtomicLong totalBytesDownloaded = new AtomicLong();
    private final AtomicLong totalFilesDownloaded = new AtomicLong();
    private volatile long totalExpectedBytes;
    private volatile long totalExpectedFiles;
    private volatile ProgressCallback bytesCallback;
    private volatile ProgressCallback filesCallback;

    public void setTotalExpected(long bytes, long files) {
        this.totalExpectedBytes = bytes;
        this.totalExpectedFiles = files;
    }

    public void setBytesCallback(ProgressCallback callback) {
        this.bytesCallback = callback;
    }

    public void setFilesCallback(ProgressCallback callback) {
        this.filesCallback = callback;
    }

    public void addBytesProgress(long bytes) {
        long newTotal = totalBytesDownloaded.addAndGet(bytes);
        if (bytesCallback != null) {
            bytesCallback.onProgress(newTotal, totalExpectedBytes);
        }
    }

    public void addFileProgress(long files) {
        long newTotal = totalFilesDownloaded.addAndGet(files);
        if (filesCallback != null) {
            filesCallback.onProgress(newTotal, totalExpectedFiles);
        }
    }

    public void completeAllProgress() {
        if (bytesCallback != null) bytesCallback.onProgress(totalExpectedBytes, totalExpectedBytes);
        if (filesCallback != null) filesCallback.onProgress(totalExpectedFiles, totalExpectedFiles);
    }

    public long getCurrentBytes() {
        return totalBytesDownloaded.get();
    }
}
