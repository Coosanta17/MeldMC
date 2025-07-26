package net.coosanta.meldmc.gui.controllers.serverselection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import net.coosanta.meldmc.utility.ResourceUtil;

import javax.annotation.Nullable;
import java.io.IOException;

public class SelectionPanel extends BorderPane {
    @FXML
    private CentrePanel centrePanel;
    @FXML
    private ButtonPanel buttonPane;
    @FXML
    private StackPane centreContainer;

    private ServerEntry selectedServer;
    private @Nullable Integer selectedServerIndex;

    public SelectionPanel() {
        loadFXML();
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtil.loadResource("/fxml/serverselection/SelectionPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            if (centrePanel != null) {
                centrePanel.setSelectionPanel(this);
                if (buttonPane != null) {
                    buttonPane.setCentrePanel(centrePanel);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for SelectionPanel", e);
        }
    }

    public void selectEntry(ServerEntry newSelection, Integer index) {
        if (selectedServer != null) {
            selectedServer.getStyleClass().remove("entry-selected");
        }

        selectedServer = newSelection;
        selectedServerIndex = index;

        if (newSelection != null) {
            selectedServer.getStyleClass().add("entry-selected");
        }

        buttonPane.serverSelected(selectedServer);
    }

    public @Nullable Integer getSelectedServerIndex() {
        return selectedServerIndex;
    }

    public CentrePanel getCentrePanel() {
        return centrePanel;
    }
}
