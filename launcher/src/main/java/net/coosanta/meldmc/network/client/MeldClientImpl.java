package net.coosanta.meldmc.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coosanta.meldmc.utility.SSLUtils;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MeldClientImpl implements MeldClient {
    private final String baseUrl;
    private final boolean isHttps;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final SSLContext sslContext;
    private ProgressCallback progressCallback;

    /**
     * Creates a new MeldClient instance.
     *
     * @param host          The hostname of the server.
     * @param port          The port of the server.
     * @param useHttps      Whether to use HTTPS or HTTP.
     * @param trustAllCerts If true, all certificates including self-signed will be trusted.
     */
    public MeldClientImpl(String host, int port, boolean useHttps, boolean trustAllCerts) {
        this.isHttps = useHttps;
        this.baseUrl = (useHttps ? "https://" : "http://") + host + ":" + port;

        // Set up SSL context
        if (useHttps && trustAllCerts) {
            try {
                this.sslContext = SSLUtils.createTrustAllSSLContext();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException("Failed to create SSL context", e);
            }
        } else {
            this.sslContext = null;
        }
    }

    @Override
    public CompletableFuture<MeldData> fetchModInfo() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = URI.create(baseUrl + "/info").toURL();
                HttpURLConnection connection = setupConnection(url, "GET");

                if (connection.getResponseCode() != 200) {
                    throw new IOException("HTTP error code: " + connection.getResponseCode());
                }

                try (InputStream is = connection.getInputStream()) {
                    return objectMapper.readValue(is, MeldData.class);
                }
            } catch (Exception e) {
                throw new CompletionException("Failed to fetch mod info", e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<List<Path>> downloadFiles(List<String> hashes, Path destination) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Files.createDirectories(destination);

                URL url = URI.create(baseUrl + "/files").toURL();
                HttpURLConnection connection = setupConnection(url, "POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                // Send hash list
                try (OutputStream os = connection.getOutputStream()) {
                    objectMapper.writeValue(os, hashes);
                }

                if (connection.getResponseCode() != 200) {
                    throw new IOException("HTTP error code: " + connection.getResponseCode());
                }

                long contentLength = connection.getContentLengthLong();
                return processZipResponse(destination, connection, contentLength);
            } catch (Exception e) {
                throw new CompletionException("Failed to download files", e);
            }
        }, executorService);
    }

    private @NotNull List<Path> processZipResponse(Path destination, HttpURLConnection connection, long contentLength) throws IOException {
        List<Path> extractedFiles = new ArrayList<>();

        try (ProgressTrackingInputStream progressStream = new ProgressTrackingInputStream(connection.getInputStream(), contentLength);
             ZipInputStream zipIn = new ZipInputStream(progressStream)) {

            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) { // No directories should be in the response.
                    zipIn.closeEntry();
                    continue;
                }

                progressStream.setCurrentFileName(entry.getName());

                Path filePath = destination.resolve(entry.getName());

                Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                extractedFiles.add(filePath);

                zipIn.closeEntry();
            }
        }
        return extractedFiles;
    }

    @Override
    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    private HttpURLConnection setupConnection(URL url, String method) throws IOException {
        HttpURLConnection connection;

        if (isHttps) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();

            // Apply custom SSL context if needed
            if (sslContext != null) {
                httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsConnection.setHostnameVerifier((hostname, session) -> true);
            }

            connection = httpsConnection;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }

        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        return connection;
    }

    /**
     * Exception thrown when a network operation fails to complete successfully.
     */
    public static class CompletionException extends RuntimeException {
        public CompletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Input stream that tracks progress and notifies callbacks.
     */
    private class ProgressTrackingInputStream extends InputStream {
        private final InputStream wrapped;
        private final long totalSize;
        private long bytesRead = 0;
        private String currentFileName;

        public ProgressTrackingInputStream(InputStream wrapped, long totalSize) {
            this.wrapped = wrapped;
            this.totalSize = totalSize;
        }

        public void setCurrentFileName(String fileName) {
            this.currentFileName = fileName;
        }

        @Override
        public int read() throws IOException {
            int b = wrapped.read();
            if (b != -1) {
                bytesRead++;
                updateProgress();
            }
            return b;
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            int bytesRead = wrapped.read(b, off, len);
            if (bytesRead > 0) {
                this.bytesRead += bytesRead;
                updateProgress();
            }
            return bytesRead;
        }

        private void updateProgress() {
            if (progressCallback != null) {
                progressCallback.onProgress(bytesRead, totalSize, currentFileName);
            }
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
}
