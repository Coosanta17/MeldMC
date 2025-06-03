package net.coosanta.meldmc.gui.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

public class SelectionPanel extends BorderPane {
    private CentrePanel centrePanel;

    public SelectionPanel() {
        setTop(createHeader());

        centrePanel = new CentrePanel();

        StackPane centreContainer = new StackPane(centrePanel);
        centreContainer.setAlignment(Pos.TOP_CENTER);
        centreContainer.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane centreScrollPane = new ScrollPane(centreContainer);

        centreScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centreScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        centreScrollPane.setFitToWidth(true);
        centreScrollPane.setFitToHeight(true);

        setCenter(centreScrollPane);

        GridPane buttonPane = new ButtonPanel();
        setBottom(buttonPane);
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
