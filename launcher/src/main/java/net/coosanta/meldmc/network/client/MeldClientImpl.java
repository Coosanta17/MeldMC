package net.coosanta.meldmc.network.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coosanta.meldmc.exceptions.GlobalExceptionHandler;
import net.coosanta.meldmc.network.ProgressCallback;
import net.coosanta.meldmc.network.ProgressTrackingInputStream;
import net.coosanta.meldmc.utility.SSLUtils;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MeldClientImpl implements MeldClient {
    private final String baseUrl;
    private final boolean isHttps;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool(
            GlobalExceptionHandler.threadFactory("meld-client")
    );
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
    public CompletableFuture<Set<Path>> downloadFiles(Collection<String> hashes, Path destination) {
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

    private @NotNull Set<Path> processZipResponse(Path destination, HttpURLConnection connection, long contentLength) throws IOException {
        Set<Path> extractedFiles = new HashSet<>();

        try (ProgressTrackingInputStream progressStream = new ProgressTrackingInputStream(connection.getInputStream(), contentLength, false, progressCallback);
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
        try {
            if (!executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
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
}
