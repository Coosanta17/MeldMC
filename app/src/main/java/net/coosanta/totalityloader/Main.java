package net.coosanta.totalityloader;

import net.coosanta.totalityloader.gui.Gui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Main {
    private static Main instance;

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    //    private final ArrayList<String> jvmArgs;
    private final Dimension windowSize;
    private final ArrayList<String> gameArgs;

    private Main(String[] args) {
        this.gameArgs = new ArrayList<>(List.of(args));

        int widthArgIndex = this.getGameArgs().indexOf("--width");
        int heightArgIndex = this.getGameArgs().indexOf("--height");

        int width;
        int height;

        if (widthArgIndex == -1 || heightArgIndex == -1) {
            // I don't know default values, but I think these are it.
            width = 600;
            height = 400;
        } else {
            try {
                width = Integer.parseInt(this.getGameArgs().get(widthArgIndex + 1));
                height = Integer.parseInt(this.getGameArgs().get(heightArgIndex + 1));
            } catch (NumberFormatException e) {
                log.error("Invalid window size arguments", e);
                width = 320;
                height = 240;
            }
        }

        this.windowSize = new Dimension(width, height);

        try {
            InputStream is = Main.class.getResourceAsStream("/mojangles.ttf");
            if (is == null) {
                throw new FileNotFoundException("Font file not found in resources.");
            }

            Font mojangles = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(mojangles);

            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get (key);
                if (value instanceof Font)
                    UIManager.put (key, mojangles);
            }
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                new Gui(windowSize);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        Main.createInstance(args);
    }

    public static void createInstance(String[] args) {
        if (instance == null) {
            instance = new Main(args);
        } else {
            log.error("Main instance already exists");
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public ArrayList<String> getGameArgs() {
        return gameArgs;
    }

//    public ArrayList<String> getJvmArgs() {
//        return jvmArgs;
//    }

    public Dimension getWindowSize() {
        return windowSize;
    }

    public Path getGameDir() {
        int gameDirArgIndex = instance.getGameArgs().indexOf("--gameDir");
        if (gameDirArgIndex == -1) throw new IllegalArgumentException("Missing gameDir argument");
        return Path.of(instance.getGameArgs().get(gameDirArgIndex + 1));
    }

}
