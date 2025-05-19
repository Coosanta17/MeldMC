package net.coosanta.meldmc.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.serverselection.SelectionPanel;
import net.coosanta.meldmc.utility.ResourceUtil;

public class MainWindow extends Application {
    private static final int DESIGN_WIDTH = Main.getWindowsSize().width;
    private static final int DESIGN_HEIGHT = Main.getWindowsSize().height;

    private final StackPane root = new StackPane();
    private SelectionPanel contentPanel;
    private Background background;

    @Override
    public void start(Stage stage) throws Exception {
        background = new Background(DESIGN_WIDTH, DESIGN_HEIGHT);
        contentPanel = new SelectionPanel();

        root.getChildren().addAll(background, contentPanel);

        Scene scene = new Scene(root, DESIGN_WIDTH, DESIGN_HEIGHT);
        scene.getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        stage.setTitle("Minecraft - Meld");
        stage.setScene(scene);
        stage.setMinWidth(DESIGN_WIDTH);
        stage.setMinHeight(DESIGN_HEIGHT);

        stage.widthProperty().addListener((obs, old, newWidth) ->
                background.resize(newWidth.doubleValue(), scene.getHeight()));

        stage.heightProperty().addListener((obs, old, newHeight) ->
                background.resize(scene.getWidth(), newHeight.doubleValue()));

        stage.setOnCloseRequest(event -> {
            javafx.application.Platform.exit();
            System.exit(0);
        });

        stage.show();
    }
}
