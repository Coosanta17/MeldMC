package net.coosanta.meldmc.gui.views.serverselection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import net.coosanta.meldmc.utility.ResourceUtil;

import java.io.IOException;

public class SelectionPanel extends BorderPane {
    @FXML
    private CentrePanel centrePanel;
    @FXML
    private ButtonPanel buttonPane;
    @FXML
    private StackPane centreContainer;

    private ServerEntry selectedServer;

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
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for SelectionPanel", e);
        }
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
