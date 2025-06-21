package net.coosanta.meldmc.gui.views.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SelectionPanel extends BorderPane {
    private final CentrePanel centrePanel;
    private final ButtonPanel buttonPane;
    private ServerEntry selectedServer;

    public SelectionPanel() {
        setTop(createHeader());

        centrePanel = new CentrePanel(this);

        StackPane centreContainer = new StackPane(centrePanel);
        centreContainer.setAlignment(Pos.TOP_CENTER);
        centreContainer.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane centreScrollPane = new ScrollPane(centreContainer);

        centreScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centreScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        centreScrollPane.setFitToWidth(true);
        centreScrollPane.setFitToHeight(true);

        setCenter(centreScrollPane);

        buttonPane = new ButtonPanel();
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

    public void selectEntry(ServerEntry newSelection) {
        if (selectedServer != null) {
            selectedServer.setBorder(null);
        }

        selectedServer = newSelection;

        selectedServer.setBorder(new Border(new BorderStroke(
                Color.WHITE,
                BorderStrokeStyle.SOLID,
                new CornerRadii(0),
                new BorderWidths(2)
        )));
        buttonPane.serverSelected(selectedServer);
    }

    public ServerEntry getSelectedServer() {
        return selectedServer;
    }
}
