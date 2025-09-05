package net.coosanta.meldmc.network;

import java.util.concurrent.atomic.AtomicLong;

public class UnifiedProgressTracker {
    private final AtomicLong totalBytesDownloaded = new AtomicLong();
    private final AtomicLong totalFilesDownloaded = new AtomicLong();

    private volatile long totalExpectedBytes;
    private volatile long totalExpectedFiles;

    private volatile ProgressCallback bytesCallback;
    private volatile ProgressCallback filesCallback;
    private volatile ProgressCallback stageCallback;
    private volatile Runnable hideCallback;

    private LaunchStage stage = LaunchStage.INITIAL;

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

    public void setStageCallback(ProgressCallback callback) {
        this.stageCallback = callback;
    }

    public void setHideCallback(Runnable callback) {
        this.hideCallback = callback;
    }

    public enum LaunchStage {
        INITIAL,
        MODS,
        LIBRARIES,
        STARTING
    }

    public void setStage(LaunchStage stage) {
        this.stage = stage;
        stageCallback.onProgress(0, 0, stage);
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
        if (hideCallback == null) {
            if (bytesCallback != null) bytesCallback.onProgress(totalExpectedBytes, totalExpectedBytes);
            if (filesCallback != null) filesCallback.onProgress(totalExpectedFiles, totalExpectedFiles);
        } else {
            hideCallback.run();
        }
    }

    public long getCurrentBytes() {
        return totalBytesDownloaded.get();
    }
}
