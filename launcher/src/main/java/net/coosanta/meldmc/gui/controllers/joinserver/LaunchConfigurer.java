package net.coosanta.meldmc.gui.controllers.joinserver;

import javafx.application.Platform;
import javafx.scene.Node;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.GameInstance;
import net.coosanta.meldmc.minecraft.InstanceManager;
import net.coosanta.meldmc.network.UnifiedProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LaunchConfigurer {
    private static final Logger log = LoggerFactory.getLogger(LaunchConfigurer.class);

    public static Node decideLaunchScreen(String address) {
        GameInstance serverInstance = InstanceManager.getInstance(address);
        if (serverInstance.getChangedMods().isEmpty() && serverInstance.getDeletedMods().isEmpty()) {
            return configureAndLaunch(serverInstance);
        } else {
            return new ModDownloadConfirmation(serverInstance);
        }
    }

    static DownloadProgressPanel configureAndLaunch(GameInstance serverInstance) {
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

        progressTracker.setHideCallback(progressPanel::hideProgressInfo);

        serverInstance.downloadModsAndLaunch(progressTracker, Main.getLaunchArgs());

        return progressPanel;
    }
}
