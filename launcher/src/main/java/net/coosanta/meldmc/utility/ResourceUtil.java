package net.coosanta.meldmc.utility;

import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

public class ResourceUtil {
    private static final Logger log = LoggerFactory.getLogger(ResourceUtil.class);

    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Map<String, AudioClip> audioCache = new HashMap<>();

    public static Image getImage(String path) {
        return imageCache.computeIfAbsent(path, p -> {
            try {
                return new Image(p);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load image: " + p, e);
            }
        });
    }

    public static AudioClip getAudio(String path) {
        return audioCache.computeIfAbsent(path, p -> {
            try {
                return new AudioClip(ResourceUtil.loadResource(p).toExternalForm());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load audio: " + p, e);
            }
        });
    }

    public static URL loadResource(String path) {
        URL resource = ResourceUtil.class.getResource(path);
        if (resource == null) {
            throw new RuntimeException("Failed to load resource '" + path + "'");
        }
        return resource;
    }

    public static Image imageFromByteArray(byte[] imageData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        return new Image(bis);
    }

    public static FXMLLoader loadFXML(String path, Object rootController) {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtil.loadResource(path));
        fxmlLoader.setRoot(rootController);
        fxmlLoader.setController(rootController);

        return fxmlLoader;
    }

    public static String calculateSHA512(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        return HexFormat.of().formatHex(hashBytes);
    }
}
