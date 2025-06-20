package net.coosanta.meldmc.gui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.serverselection.SelectionPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

import static net.coosanta.meldmc.Main.DESIGN_HEIGHT;
import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class MainWindowController {
    private static final Logger log = LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    private StackPane root;

    @FXML
    private Background background;

    @FXML
    private SelectionPanel contentPanel;

    private Stage stage;

    public void initialize() {
        log.info("MainWindow controller initialized");
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        Dimension windowDimension = Main.getWindowsSize();
        background.resize(windowDimension.width, windowDimension.height);

        stage.setTitle("Minecraft - Meld");
        stage.setMinWidth(DESIGN_WIDTH);
        stage.setMinHeight(DESIGN_HEIGHT);

        Scene scene = stage.getScene();
        scene.widthProperty().addListener((obs, old, newWidth) ->
                background.resize(newWidth.doubleValue(), scene.getHeight()));

        scene.heightProperty().addListener((obs, old, newHeight) ->
                background.resize(scene.getWidth(), newHeight.doubleValue()));

        stage.setOnCloseRequest(event -> System.exit(0));
    }
}
