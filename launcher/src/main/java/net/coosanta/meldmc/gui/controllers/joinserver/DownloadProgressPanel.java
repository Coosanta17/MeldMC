package net.coosanta.meldmc.gui.controllers.joinserver;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class DownloadProgressPanel extends VBox {
    private final ProgressBar bytesProgressBar;
    private final Label bytesLabel;
    private final Label filesLabel;
    private final Label statusLabel;

    public DownloadProgressPanel() {
        setAlignment(Pos.CENTER);
        setSpacing(10);

        statusLabel = new Label("Preparing download...");
        statusLabel.getStyleClass().add("white");

        bytesProgressBar = new ProgressBar();
        bytesProgressBar.setPrefWidth(DESIGN_WIDTH * 0.8);
        bytesProgressBar.setProgress(0);
        bytesProgressBar.getStyleClass().add("mc-progress-bar");

        bytesLabel = new Label("Downloading... 0 / 0");
        bytesLabel.getStyleClass().add("white");

        filesLabel = new Label("Files: 0 / 0");
        filesLabel.getStyleClass().add("white");

        getChildren().addAll(statusLabel, bytesProgressBar, bytesLabel, filesLabel);
    }

    public void updateBytesProgress(long downloaded, long total) {
        double progress = total > 0 ? (double) downloaded / total : 0;
        bytesProgressBar.setProgress(progress);
        bytesLabel.setText(String.format("Downloading: %s / %s", formatBytes(downloaded), formatBytes(total)));

        if (progress >= 1.0) {
            statusLabel.setText("Download complete! Launching game...");
        } else if (downloaded > 0) {
            statusLabel.setText("Downloading mods...");
        }
    }

    public void updateFilesProgress(long downloaded, long total) {
        filesLabel.setText(String.format("Files: %d / %d", downloaded, total));
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1_000_000L) return String.format("%.1f KB", bytes / 1000.0);
        return String.format("%.1f MB", bytes / 1e+6);
    }
}
