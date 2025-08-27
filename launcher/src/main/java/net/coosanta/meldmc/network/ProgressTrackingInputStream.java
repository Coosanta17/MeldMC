package net.coosanta.meldmc.network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that tracks progress and notifies callbacks.
 */
public class ProgressTrackingInputStream extends InputStream {
    private final boolean isCumulative;

    private final InputStream wrapped;
    private final long totalSize;
    private final ProgressCallback progressCallback;

    private long bytesRead = 0;
    private String currentFileName;

    public static final int PROGRESS_UPDATE_BATCH = 8192;
    private int bytesSinceLastUpdate = 0;

    public ProgressTrackingInputStream(@NotNull InputStream wrapped, long totalSize, boolean cumulative, ProgressCallback progressCallback) {
        this.wrapped = wrapped;
        this.totalSize = totalSize;
        this.progressCallback = progressCallback;
        this.isCumulative = cumulative;
    }

    public void setCurrentFileName(String fileName) {
        this.currentFileName = fileName;
    }

    @Override
    public int read() throws IOException {
        int b = wrapped.read();
        if (b != -1) {
            bytesRead++;
            bytesSinceLastUpdate++;
            if (bytesSinceLastUpdate >= PROGRESS_UPDATE_BATCH) {
                updateProgress();
            }
        }
        return b;
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        int bytesRead = wrapped.read(b, off, len);
        if (bytesRead > 0) {
            this.bytesRead += bytesRead;
            bytesSinceLastUpdate += bytesRead;
            if (bytesSinceLastUpdate >= PROGRESS_UPDATE_BATCH) {
                updateProgress();
            }
        }
        return bytesRead;
    }

    private void updateProgress() {
        if (progressCallback != null) {
            long progressValue = isCumulative ? bytesRead : bytesSinceLastUpdate;
            progressCallback.onProgress(progressValue, totalSize, currentFileName);
        }
        if (!isCumulative) {
            bytesSinceLastUpdate = 0;
        }
    }

    @Override
    public int read(byte @NotNull [] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = wrapped.skip(n);
        if (skipped > 0) {
            bytesRead += skipped;
            updateProgress();
        }
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return wrapped.available();
    }

    @Override
    public boolean markSupported() {
        return wrapped.markSupported();
    }

    @Override
    public void mark(int readlimit) {
        wrapped.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException("Reset the input stream with thy calls. I would thou couldst.");
    }

    @Override
    public void close() throws IOException {
        try {
            if (bytesSinceLastUpdate > 0) {
                updateProgress();
            }
        } finally {
            wrapped.close();
        }
    }
}
