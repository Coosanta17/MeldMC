package net.coosanta.meldmc;

import net.coosanta.meldmc.gui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            MainWindow.launch(MainWindow.class);
        } catch (Exception e) {
            logger.error("Failed to launch application", e);
        }
    }
}
