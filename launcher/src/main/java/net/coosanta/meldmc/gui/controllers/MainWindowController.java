package net.coosanta.meldmc.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.controllers.meldserverinfo.MeldInfoPanel;
import net.coosanta.meldmc.gui.views.Background;
import net.coosanta.meldmc.gui.controllers.serverselection.SelectionPanel;
import net.coosanta.meldmc.gui.views.LoadingScreen;
import net.coosanta.meldmc.minecraft.ServerInfo;
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
    private SelectionPanel selectionPanel;

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

    public void showEditServerPanel() {
        showEditServerPanel(null);
    }

    public void showEditServerPanel(Integer index) {
        log.debug("Showing Edit Server panel");
        var editServerPanel = new EditServer(index);

        showScreen(editServerPanel);
    }

    public void showMeldInfoPanel(ServerInfo server) {
        log.debug("Showing Meld Info Panel");
        var meldInfoPanel = new MeldInfoPanel(server);

        showScreen(meldInfoPanel);
    }

    public void showLoadingScreen(Node contents) {
        log.debug("Showing loading screen");
        var loadingScreen = new LoadingScreen(contents);

        showScreen(loadingScreen);
    }

    public void showSelectionPanel() {
        log.debug("Setting Selection panel");

        clearTheBoardMike();
        if (!root.getChildren().contains(selectionPanel)) {
            root.getChildren().add(selectionPanel);
        }
    }

    private void showScreen(Node screen) {
        clearTheBoardMike();
        root.getChildren().add(screen);

//        if (screen instanceof Region region) {
//            region.prefWidthProperty().bind(root.widthProperty());
//            region.prefHeightProperty().bind(root.heightProperty());
//        }
    }

    private void clearTheBoardMike() {
        root.getChildren().removeIf(node -> !(node instanceof Background));
    }

    public SelectionPanel getSelectionPanel() {
        return selectionPanel;
    }
}
