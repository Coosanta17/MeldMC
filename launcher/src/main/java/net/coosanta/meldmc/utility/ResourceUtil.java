package net.coosanta.meldmc.utility;

import java.net.URL;

public class ResourceUtil {
    public static URL loadResource(String path) {
        try {
            return ResourceUtil.class.getResource(path);
        } catch (NullPointerException e) {
            throw new RuntimeException("Failed to load resource '" + path + "'", e);
        }
    }
}
