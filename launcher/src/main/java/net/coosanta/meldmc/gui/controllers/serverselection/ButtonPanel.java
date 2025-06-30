package net.coosanta.meldmc.gui.controllers.serverselection;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.ServerListManager;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class ButtonPanel extends GridPane {
    private static final Logger log = LoggerFactory.getLogger(ButtonPanel.class);
    private CentrePanel centrePanel;

    @FXML
    private MinecraftButton joinServerButton;
    @FXML
    private MinecraftButton serverInfoButton;
    @FXML
    private MinecraftButton addServerButton;
    @FXML
    private MinecraftButton editButton;
    @FXML
    private MinecraftButton deleteButton;
    @FXML
    private MinecraftButton refreshButton;
    @FXML
    private MinecraftButton settingsButton;


    public ButtonPanel() {
        try {
            ResourceUtil.loadFXML("/fxml/serverselection/ButtonPanel.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for ButtonPanel", e);
        }
        setupEventHandlers();
        disableServerButtons();

        setPrefWidth(DESIGN_WIDTH);
        setMaxWidth(DESIGN_WIDTH);
    }

    private void setupEventHandlers() {
        joinServerButton.setOnAction(e -> handleJoinServer());
        serverInfoButton.setOnAction(e -> handleServerInfo());
        addServerButton.setOnAction(e -> handleAddServer());

        editButton.setOnAction(e -> handleEditServer());
        deleteButton.setOnAction(e -> handleDeleteServer());
        refreshButton.setOnAction(e -> handleRefresh());
        settingsButton.setOnAction(e -> handleSettings());
    }

    private void disableServerButtons() {
        joinServerButton.setDisable(true);
        serverInfoButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    public void serverSelected(ServerEntry server) {
        if (server == null) {
            disableServerButtons();
            return;
        }
        joinServerButton.setDisable(false);
        serverInfoButton.setDisable(false);
        editButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    // Placeholder methods.
    private void handleJoinServer() {
        log.debug("Join server clicked");
    }

    private void handleServerInfo() {
        log.debug("Server info clicked");
    }

    private void handleAddServer() {
        log.debug("Add server clicked");
        MainWindow.getInstance().getController().showEditServerPanel();
    }

    private Integer getSelectedServerIndex() {
        Integer selectedIndex = MainWindow.getInstance().getController().getSelectionPanel().getSelectedServerIndex();
        if (selectedIndex == null) {
            log.error("No server selected");
        }
        return selectedIndex;
    }

    private void handleEditServer() {
        log.debug("Edit clicked");
        Integer selectedIndex = getSelectedServerIndex();
        if (selectedIndex != null) {
            MainWindow.getInstance().getController().showEditServerPanel(selectedIndex);
        }
    }

    private void handleDeleteServer() {
        log.debug("Delete clicked");
        Integer selectedIndex = getSelectedServerIndex();
        if (selectedIndex != null) {
            ServerListManager.getInstance().removeServer(selectedIndex);
            refreshCentrePanel();
        }
        MainWindow.getInstance().getController().getSelectionPanel().selectEntry(null, null);
    }

    private void refreshCentrePanel() {
        if (centrePanel != null) {
            centrePanel.reload();
        } else {
            log.warn("Cannot refresh - CentrePanel reference null");
        }
    }

    private void handleRefresh() {
        log.debug("Refresh clicked");
        refreshCentrePanel();
    }

    private void handleSettings() {
        log.debug("Settings clicked");
    }

    void setCentrePanel(CentrePanel centrePanel) {
        this.centrePanel = centrePanel;
    }
}