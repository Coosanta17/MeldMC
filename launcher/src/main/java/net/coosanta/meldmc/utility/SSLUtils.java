package net.coosanta.meldmc.utility;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Utilities for SSL/TLS configuration.
 */
public final class SSLUtils {

    private SSLUtils() {
    }

    /**
     * Creates an SSL context that accepts self-signed certificates.
     *
     * @return The configured SSLContext
     */
    public static SSLContext createTrustAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }

    /**
     * Creates a custom hostname verifier for SSL connections.
     *
     * @param expectedHostname The expected hostname to verify against
     * @return A HostnameVerifier that validates against the expected hostname
     */
    public static HostnameVerifier createHostnameVerifier(String expectedHostname) {
        return (hostname, session) -> hostname.equals(expectedHostname);
    }
}
