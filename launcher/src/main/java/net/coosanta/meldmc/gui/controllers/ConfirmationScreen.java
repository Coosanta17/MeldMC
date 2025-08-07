package net.coosanta.meldmc.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.utility.ResourceUtil;

import java.io.IOException;

public class ConfirmationScreen extends StackPane {
    @FXML
    private StackPane confirmation;
    @FXML
    private HBox buttonsBox;

    public ConfirmationScreen(Node confirmation, MinecraftButton... buttons) {
        try {
            ResourceUtil.loadFXML("/fxml/ConfirmationScreen.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.buttonsBox.setPrefWidth(getWidth());
        this.buttonsBox.getChildren().addAll(buttons);

        this.confirmation.getChildren().add(confirmation);
    }
}