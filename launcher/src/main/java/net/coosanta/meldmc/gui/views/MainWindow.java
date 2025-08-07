package net.coosanta.meldmc.gui.views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.controllers.MainWindowController;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.IOException;

public class MainWindow extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    private static final Dimension windowDimension = Main.getWindowsSize();

    private static MainWindow instance;

    private Stage stage;
    private MainWindowController controller;

    public MainWindow() {
        // Constructor required by JavaFX
    }

    /**
     * Gets the singleton instance of MainWindow
     *
     * @return The MainWindow instance
     */
    public static MainWindow getInstance() {
        return instance;
    }

    public MainWindowController getController() {
        return controller;
    }

    @Override
    public void start(Stage stage) throws IOException {
        log.info("Starting application main window");

        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(ResourceUtil.loadResource("/fxml/MainWindow.fxml"));
        StackPane root = loader.load();

        controller = loader.getController();

        Scene scene = new Scene(root, windowDimension.width, windowDimension.height);
        scene.getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        stage.setScene(scene);

        controller.setStage(stage);

        instance = this;

        controller.getSelectionPanel().getButtonPane().setMainController(controller);

        stage.show();
    }
}
