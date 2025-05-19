package net.coosanta.meldmc.gui.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import net.coosanta.meldmc.Main;

public class SelectionPanel extends BorderPane {
    private static final int DESIGN_WIDTH = Main.getWindowsSize().width;
    private static final int DESIGN_HEIGHT = Main.getWindowsSize().height;

    public SelectionPanel() {
        setTop(createHeader());
        setCenter(new CentrePanel());
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
