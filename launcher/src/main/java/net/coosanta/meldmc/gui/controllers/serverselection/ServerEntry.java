package net.coosanta.meldmc.gui.controllers.serverselection;

import javafx.application.Platform;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import net.coosanta.meldmc.gui.nodes.text.FormattedTextParser;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.network.Pinger;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class ServerEntry extends BorderPane implements ScaleFactorCssProperty.ScaleFactorContainer {
    private final Label unknownLabel = new Label("unknown");
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ScaleFactorCssProperty scaleFactorProperty;

    private final ServerInfo server;

    @FXML
    private ImageView icon;
    private final int defaultIconSize = 64;
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

    public ServerEntry(ServerInfo server, ExecutorService pingTask) {
        this.server = server;
        this.scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");

        loadFXML();

        scaleFactorProperty.property().addListener((obs, oldVal, newVal) ->
                updateIconSize());

        setupUI();

        Platform.runLater(this::updateIconSize);
        pingTask.submit(() -> Pinger.ping(server, this));
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceUtil.loadResource("/fxml/serverselection/ServerEntry.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for ServerEntry", e);
        }
    }

    private void setupUI() {
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
    }

    private void updateIconSize() {
        double scaledIconSize = defaultIconSize * getScaleFactor();
        icon.setFitWidth(scaledIconSize);
        icon.setFitHeight(scaledIconSize);
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

            online.getStyleClass().add("player-number");
            separator.getStyleClass().add("player-separator");
            maxPlayers.getStyleClass().add("player-number");

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
            // TODO: Modern minecraft styling.
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
            icon.setImage(ResourceUtil.imageFromByteArray(server.getFavicon()));
        }

        updateMotd();
    }

    @Override
    public StyleableProperty<Number> getScaleFactorProperty() {
        return scaleFactorProperty.property();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(BorderPane.getClassCssMetaData());
        styleables.add(ScaleFactorCssProperty.getCssMetaData());
        return Collections.unmodifiableList(styleables);
    }

    private double getScaleFactor() {
        return scaleFactorProperty.get();
    }
}
