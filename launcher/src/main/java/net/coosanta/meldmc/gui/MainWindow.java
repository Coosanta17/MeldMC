package net.coosanta.meldmc.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.utility.ResourceUtil;

public class MainWindow extends Application {
    private static final int DESIGN_WIDTH = 854;
    private static final int DESIGN_HEIGHT = 480;

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane root = new BorderPane();

        Scene scene = new Scene(root, DESIGN_WIDTH, DESIGN_HEIGHT);

        scene.getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        stage.setTitle("Meld - Select Server");
        stage.setScene(scene);
        stage.setMinWidth(DESIGN_WIDTH);
        stage.setMinHeight(DESIGN_HEIGHT);

        root.setTop(createHeader());

        stage.show();
    }

    private Node createHeader() {
        Label headerText = new Label("Select Server 你好你很胖");
        headerText.getStyleClass().add("header");

        StackPane headerPane = new StackPane(headerText);
        headerPane.setAlignment(Pos.CENTER);
        headerPane.setPadding(new Insets(5));

        return headerPane;
    }
}
