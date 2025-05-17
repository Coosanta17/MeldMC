package net.coosanta.meldmc.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.utility.ResourceUtil;

public class MainWindow extends Application {
    private static final int DESIGN_WIDTH = 854;
    private static final int DESIGN_HEIGHT = 480;
    private static final int TILE_SIZE = 100;

    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();

        Scene scene = new Scene(root, DESIGN_WIDTH, DESIGN_HEIGHT);
        scene.getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        // I thought css will make this unnecessary but JavaFX cannot handle pixel art. (https://bugs.openjdk.org/browse/JDK-8211861)
        Canvas background = createBackgroundCanvas();

        BorderPane content = new BorderPane();
        content.setTop(createHeader());

        root.getChildren().add(background);
        root.getChildren().add(content);

        stage.setTitle("Meld - Select Server");
        stage.setScene(scene);
        stage.setMinWidth(DESIGN_WIDTH);
        stage.setMinHeight(DESIGN_HEIGHT);

        stage.widthProperty().addListener((obs, old, newWidth) -> {
            background.setWidth(newWidth.doubleValue());
            redrawBackground(background);
        });

        stage.heightProperty().addListener((obs, old, newHeight) -> {
            background.setHeight(newHeight.doubleValue());
            redrawBackground(background);
        });

        stage.show();
    }

    private Canvas createBackgroundCanvas() {
        Canvas canvas = new Canvas(DESIGN_WIDTH, DESIGN_HEIGHT);
        redrawBackground(canvas);
        return canvas;
    }

    private void redrawBackground(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Image backgroundImage = new Image(ResourceUtil.loadResource("/icons/background.png").toExternalForm());

        for (int x = 0; x < canvas.getWidth(); x += TILE_SIZE) {
            for (int y = 0; y < canvas.getHeight(); y += TILE_SIZE) {
                gc.drawImage(backgroundImage, x, y, TILE_SIZE, TILE_SIZE);
            }
        }
        gc.setImageSmoothing(false);
    }

    private Node createHeader() {
        Label headerText = new Label("Select Server");
        headerText.getStyleClass().add("header");

        StackPane headerPane = new StackPane(headerText);
        headerPane.setAlignment(Pos.CENTER);
        headerPane.setPadding(new Insets(5));

        return headerPane;
    }
}
