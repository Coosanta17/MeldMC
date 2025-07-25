package net.coosanta.meldmc.gui.views;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class LoadingScreen extends StackPane {
    private Node contents;

    public LoadingScreen(Node contents) {
        this.contents = contents;

        getChildren().add(contents);
    }

    public LoadingScreen() {
        this(new Region());
    }

    public void setContents(Node contents) {
        this.contents = contents;
    }

    public Node getContents() {
        return contents;
    }
}
