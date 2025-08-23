package net.coosanta.meldmc.minecraft.launcher;

import net.coosanta.meldmc.network.ProgressCallback;
import net.coosanta.meldmc.network.ProgressTrackingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileDownloader {
    public static void downloadFile(String urlString, Path destination) throws IOException {
        downloadFile(urlString, destination, null);
    }

    public static void downloadFile(String urlString, Path destination, ProgressCallback progressCallback) throws IOException {
        Files.createDirectories(destination.getParent());

        URI uri = URI.create(urlString);
        URLConnection connection = uri.toURL().openConnection();

        long contentLength = -1;
        if (connection instanceof HttpURLConnection) {
            contentLength = connection.getContentLengthLong();
        }

        try (InputStream in = connection.getInputStream()) {
            InputStream downloadStream = in;

            if (progressCallback != null && contentLength > 0) {
                var trackingStream = new ProgressTrackingInputStream(in, contentLength, true, progressCallback);
                trackingStream.setCurrentFileName(destination.getFileName().toString());
                downloadStream = trackingStream;
            }

            Files.copy(downloadStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
