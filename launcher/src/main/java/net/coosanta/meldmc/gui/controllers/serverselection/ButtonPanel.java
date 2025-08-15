package net.coosanta.meldmc.gui.controllers.serverselection;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import net.coosanta.meldmc.gui.controllers.ConfirmationScreen;
import net.coosanta.meldmc.gui.controllers.MainWindowController;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.minecraft.GameInstance;
import net.coosanta.meldmc.minecraft.InstanceManager;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.minecraft.ServerListManager;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class ButtonPanel extends GridPane {
    private static final Logger log = LoggerFactory.getLogger(ButtonPanel.class);
    private CentrePanel centrePanel;

    private MainWindowController mainController;

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

    public void setMainController(MainWindowController controller) {
        this.mainController = controller;
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

    public void disableServerButtons() {
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

        Tooltip.uninstall(serverInfoButton, null);

        // FIXME: Tooltips not working properly
        GameInstance instance = InstanceManager.getInstance(server.getServer().getAddress());
        serverInfoButton.setDisable(false);
        if (instance == null || !instance.isMeldCached()) {
            if (server.getServer().getMeldData() == null) {
                serverInfoButton.setDisable(true);
                var tooltip = new Tooltip("Failed to get mod data!");
                tooltip.getStyleClass().add("mc-tooltip-error");
                Tooltip.install(serverInfoButton, tooltip);
            } else if (!server.getServer().isMeldSupported()) {
                serverInfoButton.setDisable(true);
                var tooltip = new Tooltip("Server does not support meld.");
                tooltip.getStyleClass().add("mc-tooltip");
                Tooltip.install(serverInfoButton, tooltip);
            }
        }
        editButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    // Placeholder methods.
    private void handleJoinServer() {
        log.debug("Join server clicked");
    }

    private void handleServerInfo() {
        log.debug("Server info clicked");
        ServerInfo server = getSelectedServerInfo();
        mainController.showMeldInfoPanel(server);
    }

    private void handleAddServer() {
        log.debug("Add server clicked");
        mainController.showEditServerPanel();
    }

    private void handleEditServer() {
        log.debug("Edit clicked");
        int selectedIndex = getSelectedServerIndex();
        mainController.showEditServerPanel(selectedIndex);
    }

    private void handleDeleteServer() {
        log.debug("Delete clicked");
        ServerInfo selectedServerInfo = getSelectedServerInfo();
        String warning;
        if (ServerListManager.getInstance().getServers().stream()
                    .filter(server -> server.equals(selectedServerInfo)).count() > 1) {
            warning = """
                    You are about to delete the server "%s" at '%s'.
                    Instance files will not be deleted because duplicate server(s) were detected (same address).
                    Do you wish to proceed?
                    """
                    .formatted(
                            selectedServerInfo.getName(),
                            selectedServerInfo.getAddress()
                    );
        } else {
            warning = """
                    You are about to delete the server "%s" at '%s' and all server instance files located in '%s' PERMANENTLY!
                    This includes singleplayer worlds, mods and any mod data. Data stored on the server will not be affected.
                    Do you wish to proceed?
                    """
                    .formatted(
                            selectedServerInfo.getName(),
                            selectedServerInfo.getAddress(),
                            InstanceManager.getInstance(selectedServerInfo.getAddress()).getInstanceDir().toString()
                    );
        }
        Text warningText = new Text(warning);
        warningText.getStyleClass().add("white");
        TextFlow warningTextFlow = new TextFlow(warningText);
        warningTextFlow.getStyleClass().add("white");
        warningTextFlow.setTextAlignment(TextAlignment.CENTER);

        mainController.showScreen(new ConfirmationScreen(
                warningTextFlow,
                MinecraftButton.createButton("Cancel", e -> mainController.showSelectionPanel()),
                MinecraftButton.createButton("Delete without backup", e -> deleteServer(false)),
                MinecraftButton.createButton("Backup and delete", e -> deleteServer(true))));

    }

    private void deleteServer(boolean createBackup) {
        ServerListManager.getInstance().removeServer(getSelectedServerIndex(), createBackup);
        refreshCentrePanel();
        mainController.getSelectionPanel().deselectEntries();
        mainController.showSelectionPanel();
    }

    private void handleRefresh() {
        log.debug("Refresh clicked");
        refreshCentrePanel();
    }

    private void handleSettings() {
        log.debug("Settings clicked");
    }

    private ServerInfo getSelectedServerInfo() {
        return ServerListManager.getInstance().getServers().get(getSelectedServerIndex());
    }

    private int getSelectedServerIndex() {
        Integer selectedIndex = mainController.getSelectionPanel().getSelectedServerIndex();
        if (selectedIndex == null) {
            throw new IllegalStateException("No server selected");
        }
        return selectedIndex;
    }

    private void refreshCentrePanel() {
        if (centrePanel != null) {
            centrePanel.reload();
        } else {
            log.warn("Cannot refresh - CentrePanel reference null");
        }
    }

    void setCentrePanel(CentrePanel centrePanel) {
        this.centrePanel = centrePanel;
    }
}