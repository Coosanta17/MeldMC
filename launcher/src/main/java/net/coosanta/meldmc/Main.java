package net.coosanta.meldmc;

import net.coosanta.meldmc.exceptions.GlobalExceptionHandler;
import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.launcher.LaunchArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final int DESIGN_WIDTH = 850;
    public static final int DESIGN_HEIGHT = 500;
    public static int SCALE_FACTOR = 1;

    private static Dimension windowsSize;
    private static LaunchArgs launchArgs;

    public static void main(String[] args) {
        if (args.length == 0) throw new IllegalArgumentException("No launch arguments provided");
        try {
            launchArgs = LaunchArgs.parse(args);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse launch args", e);
        }

        windowsSize = new Dimension(launchArgs.getWidth(), launchArgs.getHeight());

        try {
            GlobalExceptionHandler.installGlobal();
            MainWindow.launch(MainWindow.class);
        } catch (Exception e) {
            log.error("Failed to launch application", e);
        }
    }

    public static Dimension getWindowsSize() {
        return new Dimension(windowsSize);
    }

    public static LaunchArgs getLaunchArgs() {
        return launchArgs;
    }
}
