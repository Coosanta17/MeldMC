package net.coosanta.meldmc;

import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.launcher.LaunchArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final int DESIGN_WIDTH = 850;
    public static final int DESIGN_HEIGHT = 500;
    public static int SCALE_FACTOR = 1;

    private static Dimension windowsSize;
    private static List<String> gameArgs;
    private static LaunchArgs launchArgs;

    public static void main(String[] args) {
        gameArgs = new ArrayList<>(List.of(args));
        try {
            launchArgs = LaunchArgs.parse(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse launch args", e);
        }

        windowsSize = new Dimension(launchArgs.getWidth(), launchArgs.getHeight());

        try {
            MainWindow.launch(MainWindow.class);
        } catch (Exception e) {
            log.error("Failed to launch application", e);
        }
    }

    public static Dimension getWindowsSize() {
        return new Dimension(windowsSize);
    }

    public static List<String> getGameArgs() {
        return new ArrayList<>(gameArgs);
    }

    public static Path getGameDir() {
//        int gameDirArgIndex = getGameArgs().indexOf("--gameDir");
//        if (gameDirArgIndex == -1) throw new IllegalArgumentException("Missing gameDir argument");
//        return Path.of(getGameArgs().get(gameDirArgIndex + 1));

//        return launchArgs.getGameDir();

        // Debug for windows: TODO: REMOVE
        String appdata = System.getenv("APPDATA");
        if (appdata == null) {
            throw new IllegalStateException("APPDATA environment variable is not set");
        }
        return Path.of(appdata, ".minecraft");
    }
}
