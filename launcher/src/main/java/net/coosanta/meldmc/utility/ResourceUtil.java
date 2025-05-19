package net.coosanta.meldmc.utility;

import io.jsonwebtoken.io.IOException;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.net.URL;

public class ResourceUtil {
    public static URL loadResource(String path) {
        URL resource = ResourceUtil.class.getResource(path);
        if (resource == null) {
            throw new IOException("Failed to load resource '" + path + "'");
        }
        return resource;
    }

    public static Image imageFromByteArray(byte[] imageData) {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        return new Image(bis);
    }
}
