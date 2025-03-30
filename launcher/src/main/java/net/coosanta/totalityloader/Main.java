package net.coosanta.totalityloader;

import net.coosanta.totalityloader.gui.GuiFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class Main {
    private static Main instance;

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final Dimension windowSize;
    private final List<String> gameArgs;

    private GuiFrame guiFrame;

    private Main(String[] args) {
        this.gameArgs = new ArrayList<>(List.of(args));

        int widthArgIndex = this.getGameArgs().indexOf("--width");
        int heightArgIndex = this.getGameArgs().indexOf("--height");

        int width = 850;
        int height = 500;

        try {
            if (widthArgIndex != -1 && heightArgIndex != -1) {
                width = Integer.parseInt(this.getGameArgs().get(widthArgIndex + 1));
                height = Integer.parseInt(this.getGameArgs().get(heightArgIndex + 1));
            }
        } catch (NumberFormatException e) {
            log.error("Invalid window size arguments", e);
        }

        this.windowSize = new Dimension(width, height);

        SwingUtilities.invokeLater(() -> {
            try {
                this.guiFrame = new GuiFrame(windowSize);
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

    public List<String> getGameArgs() {
        return Collections.unmodifiableList(gameArgs);
    }

    public Dimension getWindowSize() {
        return windowSize;
    }

    public Path getGameDir() {
        int gameDirArgIndex = instance.getGameArgs().indexOf("--gameDir");
        if (gameDirArgIndex == -1) throw new IllegalArgumentException("Missing gameDir argument");
        return Path.of(instance.getGameArgs().get(gameDirArgIndex + 1));
    }

    public GuiFrame getGuiFrame() {
        return guiFrame;
    }
}
