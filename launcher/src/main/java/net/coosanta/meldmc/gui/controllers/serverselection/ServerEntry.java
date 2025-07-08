package net.coosanta.meldmc.gui.controllers.serverselection;

import javafx.application.Platform;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import net.coosanta.meldmc.gui.nodes.text.FormattedTextParser;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.minecraft.ServerListManager;
import net.coosanta.meldmc.network.Pinger;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class ServerEntry extends BorderPane implements ScaleFactorCssProperty.ScaleFactorContainer {
    private final Label unknownLabel = new Label("unknown");
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ScaleFactorCssProperty scaleFactorProperty;

    private final int index;
    private final ServerInfo server;

    @FXML
    private ImageView icon;
    private final int defaultIconSize = 64;
    private String lastFaviconHash;
    @FXML
    private Label name;
    @FXML
    private Label ping;
    @FXML
    private HBox playercount;
    @FXML
    private GridPane header;
    @FXML
    private TextFlow motdFlow;
    @FXML
    private ImageView supportIndicator;
    private final Dimension defaultSupportIndicatorSize = new Dimension(9, 8);

    public ServerEntry(int serverIndex, ExecutorService pingTask) {
        this.index = serverIndex;
        this.server = ServerListManager.getInstance().getServers().get(index);
        this.scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");

        loadFXML();

        scaleFactorProperty.property().addListener((obs, oldVal, newVal) ->
                updateIconSize());

        setupUI();

        Platform.runLater(this::updateIconSize);
        pingTask.submit(() -> Pinger.ping(server, this));
    }

    private void loadFXML() {
        try {
            ResourceUtil.loadFXML("/fxml/serverselection/ServerEntry.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for ServerEntry", e);
        }
    }

    private void setupUI() {
        // TODO: Name length limit, Description length limit, component text formatting.
        name.setText(server.getName());

        ping.setText("Pinging...");

        playercount.getChildren().add(unknownLabel);
        unknownLabel.getStyleClass().add("player-separator");

        motdFlow.getStyleClass().add("server-motd");
        updateMotd();

        Image rawIcon;
        @Nullable byte[] favicon = server.getFavicon();
        if (favicon != null) { // TODO: Favicon from server ping.
            rawIcon = ResourceUtil.imageFromByteArray(favicon);
        } else {
            rawIcon = ResourceUtil.getImage("/icons/unknown_server.png");
        }
        icon.setImage(rawIcon);

        setupSupportIndicator();
    }

    private void setupSupportIndicator() {
        if (server.isMeldSupported()) {
            supportIndicator.setImage(ResourceUtil.getImage("/icons/checkmark.png"));
        }

        Tooltip tooltip = new Tooltip("This server is compatible with Meld.");
        tooltip.getStyleClass().add("mc-tooltip");

        supportIndicator.setVisible(false);

        Tooltip.install(supportIndicator, tooltip);
    }

    private void updateIconSize() {
        final double scaledIconSize = defaultIconSize * getScaleFactor();
        icon.setFitWidth(scaledIconSize);
        icon.setFitHeight(scaledIconSize);

        updateSupportIconSize();
    }

    private void updateSupportIconSize() {
        final double scaleFactor = getScaleFactor();
        supportIndicator.setFitWidth(defaultSupportIndicatorSize.width * scaleFactor);
        supportIndicator.setFitHeight(defaultSupportIndicatorSize.height * scaleFactor);
    }

    private void updatePlayerCount() {
        playercount.getChildren().clear();
        PlayerInfo serverPlayers = server.getPlayers();
        if (serverPlayers == null) {
            playercount.getChildren().add(unknownLabel);
        } else {
            Label online = new Label(String.valueOf(serverPlayers.getOnlinePlayers()));
            Label separator = new Label("/");
            Label maxPlayers = new Label(String.valueOf(serverPlayers.getMaxPlayers()));

            online.getStyleClass().add("body");
            separator.getStyleClass().add("player-separator");
            maxPlayers.getStyleClass().add("body");

            playercount.getChildren().addAll(
                    online,
                    separator,
                    maxPlayers
            );
        }
    }

    private void updateMotd() {
        Component descriptionComp = server.getDescription();

        if (descriptionComp == null) {
            if (server.getStatus() == ServerInfo.Status.PINGING || server.getStatus() == ServerInfo.Status.INITIAL) {
                FormattedTextParser.updateTextFlowWithStatus(motdFlow, "Pinging...", "server-motd-pinging");
            } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
                FormattedTextParser.updateTextFlowWithStatus(motdFlow, "Cannot connect to server", "pinger-motd-ping-error");
            }
        } else {
            FormattedTextParser.updateTextFlow(motdFlow, descriptionComp, "server-motd");
        }
    }

    public void updateComponents() {
        updatePlayerCount();

        if (server.getStatus() == ServerInfo.Status.SUCCESSFUL) {
            ping.setText(server.getPing() + " ms");
        } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
            ping.setText("-- ms");
        }

        if (server.getFavicon() != null) {
            String newFaviconHash = Arrays.toString(server.getFavicon());
            if (!newFaviconHash.equals(lastFaviconHash)) {
                icon.setImage(ResourceUtil.imageFromByteArray(server.getFavicon()));
                lastFaviconHash = newFaviconHash;
                ServerListManager.getInstance().updateServer(index, server);
            }
        }

        if (server.isMeldSupported()) {
            supportIndicator.setImage(ResourceUtil.getImage("/icons/checkmark.png"));
            updateSupportIconSize();
            supportIndicator.setVisible(true);
        } else {
            supportIndicator.setVisible(false);
        }

        updateMotd();
    }

    @Override
    public StyleableProperty<Number> getScaleFactorProperty() {
        return scaleFactorProperty.property();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(BorderPane.getClassCssMetaData());
        styleables.add(ScaleFactorCssProperty.getCssMetaData());
        return Collections.unmodifiableList(styleables);
    }

    private double getScaleFactor() {
        return scaleFactorProperty.get();
    }
}
