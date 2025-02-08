package net.coosanta.totalityloader;

import com.mojang.logging.LogUtils;
import net.coosanta.totalityloader.gui.Gui;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Main instance;

    private static final Logger LOGGER = LogUtils.getLogger();

    // Contains session tokens, so I made it private even though it can be an inconvenience. (I don't want to think about reflection!!)
    private final ArrayList<String> jvmArgs;
    private final Dimension windowSize;
    private final ArrayList<String> gameArgs;

    private Main(String[] args) {
        int widthArgIndex = this.getGameArgs().indexOf("--width");
        int heightArgIndex = this.getGameArgs().indexOf("--height");

        int width;
        int height;

        if (widthArgIndex == -1 || heightArgIndex == -1) {
            // idk default values but I think these are it.
            width = 320;
            height = 240;
        } else {
            try {
                width = Integer.parseInt(this.getGameArgs().get(widthArgIndex + 1));
                height = Integer.parseInt(this.getGameArgs().get(heightArgIndex + 1));
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid window size arguments", e);
                width = 320;
                height = 240;
            }
        }

        this.windowSize = new Dimension(width, height);
        this.jvmArgs =
        new ArrayList<>(
            List.of(
                    ProcessHandle.current().info().commandLine().orElseThrow().split("\\s+")
            )
        );
        this.gameArgs = new ArrayList<>(List.of(args));
    }

    private void initialise() {
        SwingUtilities.invokeLater(() -> {
            try {
                new Gui(windowSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        Main mainInstance = Main.createInstance(args);
        mainInstance.initialise();
    }

    public static Main createInstance(String[] args) {
        if (instance == null) {
            instance = new Main(args);
            return instance;
        } else {
            throw new RuntimeException("Main instance already exists!");
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public ArrayList<String> getGameArgs() {
        return gameArgs;
    }

    public ArrayList<String> getJvmArgs() {
        return jvmArgs;
    }

    public Dimension getWindowSize() {
        return windowSize;
    }

    public Path getGameDir() {
        int gameDirArgIndex = instance.getGameArgs().indexOf("--gameDir");
        if (gameDirArgIndex == -1) throw new IllegalArgumentException("Missing gameDir argument");
        return Path.of(instance.getGameArgs().get(gameDirArgIndex + 1));
    }

}
