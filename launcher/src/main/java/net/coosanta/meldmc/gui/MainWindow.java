package net.coosanta.meldmc.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.serverselection.SelectionPanel;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;

import static net.coosanta.meldmc.Main.DESIGN_HEIGHT;
import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class MainWindow extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private static final Dimension windowDimension = Main.getWindowsSize();

    private final StackPane root = new StackPane();
    private SelectionPanel contentPanel;
    private Background background;

    @Override
    public void start(Stage stage) throws Exception {
        background = new Background(windowDimension.width, windowDimension.height);
        contentPanel = new SelectionPanel();

        root.getChildren().addAll(background, contentPanel);

        Scene scene = new Scene(root, windowDimension.width, windowDimension.height);
        scene.getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        stage.setTitle("Minecraft - Meld");
        stage.setScene(scene);
        stage.setMinWidth(DESIGN_WIDTH);
        stage.setMinHeight(DESIGN_HEIGHT);

        scene.widthProperty().addListener((obs, old, newWidth) ->
                background.resize(newWidth.doubleValue(), scene.getHeight()));

        scene.heightProperty().addListener((obs, old, newHeight) ->
                background.resize(scene.getWidth(), newHeight.doubleValue()));

        stage.setOnCloseRequest(event -> {
            javafx.application.Platform.exit();
            System.exit(0);
        });

        stage.show();
    }
}
