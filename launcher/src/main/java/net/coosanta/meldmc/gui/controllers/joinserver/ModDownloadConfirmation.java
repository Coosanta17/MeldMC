package net.coosanta.meldmc.gui.controllers.joinserver;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.GameInstance;
import net.coosanta.meldmc.minecraft.InstanceManager;
import net.coosanta.meldmc.network.UnifiedProgressTracker;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class ModDownloadConfirmation extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(ModDownloadConfirmation.class);
    private final GameInstance serverInstance;

    @FXML
    private Label warning;

    @FXML
    private TitledPane modrinthTitle;
    @FXML
    private TitledPane serverSentTitle;
    @FXML
    private TitledPane untrustedTitle;

    @FXML
    private VBox newModrinthMods;
    @FXML
    private VBox newServerMods;
    @FXML
    private VBox newUntrustedMods;

    @FXML
    private MinecraftButton confirm;
    @FXML
    private MinecraftButton cancel;

    // TODO: What if the user didn't acknowledge the changed mods but then it updates and thinks that the use did???
    public ModDownloadConfirmation(String address) { // TODO Mod deleting information too
        setPrefWidth(DESIGN_WIDTH);
        setMaxWidth(DESIGN_WIDTH);

        serverInstance = InstanceManager.getInstance(address);
        if (serverInstance.getChangedMods().isEmpty()) confirmClicked(null);

        try {
            ResourceUtil.loadFXML("/fxml/joinserver/ModDownloadConfirmation.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cancel.setOnAction(this::cancelClicked);
        confirm.setOnAction(this::confirmClicked);

        warning.setText(buildWarningMessage(address));

        serverInstance.getChangedMods().values().forEach(mod -> {
            switch (mod.modSource()) {
                case MODRINTH -> newModrinthMods.getChildren().add(new ModSummary(mod));
                case CURSEFORGE -> throw new IllegalArgumentException("CurseForge not supported (yet)");
                case SERVER -> newServerMods.getChildren().add(new ModSummary(mod));
                case UNTRUSTED -> newUntrustedMods.getChildren().add(new ModSummary(mod));
            }
        });

        modifyNodeVisibility(modrinthTitle, !newModrinthMods.getChildren().isEmpty());
        modifyNodeVisibility(serverSentTitle, !newServerMods.getChildren().isEmpty());
        modifyNodeVisibility(untrustedTitle, !newUntrustedMods.getChildren().isEmpty());
    }

    private @NotNull String buildWarningMessage(String address) {
        int changed = serverInstance.getChangedMods().size();
        return """
                You are about to download and run %d new or updated mod%s requested by the Minecraft server at address '%s'.
                
                Mods are executable code. They can access your files, network, and other software, and can cause serious damage if malicious.
                
                Proceed only if you trust the server operator.
                
                Be especially cautious about mods automatically sent by the server or from links other than Modrinth or CurseForge.
                
                MeldMC and Mojang do not review or endorse these mods and are not liable for any damage.
                """.formatted(changed, changed == 1 ? "" : "s", address);
    }

    private void modifyNodeVisibility(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void confirmClicked(ActionEvent event) {
        log.debug("Confirm and join server button clicked");

        var progressPanel = new DownloadProgressPanel();
        MainWindow.getInstance().getController().showScreen(progressPanel);

        var progressTracker = new UnifiedProgressTracker();

        progressTracker.setBytesCallback((downloaded, total, unused) ->
                Platform.runLater(() -> progressPanel.updateBytesProgress(downloaded, total)));

        progressTracker.setFilesCallback((downloaded, total, unused) ->
                Platform.runLater(() -> progressPanel.updateFilesProgress(downloaded, total)));

        progressTracker.setStageCallback((ø, æ, stage) ->
                Platform.runLater(() -> progressPanel.setStatusMessage(switch ((UnifiedProgressTracker.LaunchStage) ((Object[]) stage)[0]) {
                    case INITIAL -> "Starting download...";
                    case MODS -> "Downloading mods...";
                    case LIBRARIES -> "Downloading libraries...";
                    case STARTING -> "Launching the game...";
                }))
        );

        serverInstance.downloadModsAndLaunch(progressTracker, Main.getLaunchArgs());
    }

    private void cancelClicked(ActionEvent event) {
        log.debug("Cancel server clicked");
//
//        MainWindow.getInstance().getController().showMeldInfoPanel();
    }
}
