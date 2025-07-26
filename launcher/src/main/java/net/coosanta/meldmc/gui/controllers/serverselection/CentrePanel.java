package net.coosanta.meldmc.gui.controllers.serverselection;

import javafx.scene.layout.VBox;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.minecraft.ServerListManager;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.coosanta.meldmc.Main.DESIGN_HEIGHT;
import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class CentrePanel extends VBox {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService pingTask;
    private final Path gameDir = Main.getGameDir();
    private SelectionPanel selectionPanel;
    private List<ServerEntry> serverList;

    public CentrePanel() {
        // FIXME: non-hardcoded value. (50 is subtracted to account for borderwidth when server is selected)
        setPrefSize(DESIGN_WIDTH - 50, DESIGN_HEIGHT);
        setMaxSize(DESIGN_WIDTH - 50, DESIGN_HEIGHT);

        setManaged(true);

        pingTask = newPingtask();

        loadFXML();
        loadServers();
    }

    private ExecutorService newPingtask() {
        return Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
    }

    private void loadFXML() {
        try {
            ResourceUtil.loadFXML("/fxml/serverselection/CentrePanel.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for CentrePanel", e);
        }
    }

    private void loadServers() {
        serverList = getServersFromManager();
        getChildren().addAll(serverList);
    }

    private ArrayList<ServerEntry> getServersFromManager() {
        ArrayList<ServerEntry> serverList = new ArrayList<>();
        ServerListManager manager = ServerListManager.getInstance();

        List<ServerInfo> servers = manager.getServers();
        for (int i = 0; i < servers.size(); i++) {
            ServerEntry entry = new ServerEntry(i, pingTask);
            int index = serverList.size();
            entry.setOnMouseClicked(event -> selectionPanel.selectEntry(entry, index));
            serverList.add(entry);
        }

        return serverList;
    }


    void setSelectionPanel(SelectionPanel selectionPanel) {
        if (this.selectionPanel == null) {
            this.selectionPanel = selectionPanel;
        } else {
            log.warn("Tried to set selectionPanel when it was already set.");
        }
    }

    public void reload() {
        shutDownPingTasks();
        pingTask = newPingtask();
        getChildren().clear();
        loadServers();
    }

    private void shutDownPingTasks() {
        if (pingTask != null && !pingTask.isShutdown()) {
            pingTask.shutdownNow();
        }
    }

    public void dispose() {
        shutDownPingTasks();
    }
}
