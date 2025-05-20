package net.coosanta.meldmc.gui.serverselection;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.network.Pinger;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;

public class ServerEntry extends BorderPane {
    private static final Label unknownLabel = new Label("unknown");
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ServerInfo server;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final ImageView icon;
    private final Label name;
    private final Label ping;
    private final HBox playercount;
    private final GridPane header;
    private final Label motd;

    public ServerEntry(ServerInfo server, ExecutorService pingTask) {
        setPadding(new Insets(5, 10, 5, 10));

        this.server = server;

        this.name = new Label(server.getName());
        this.ping = new Label("Pinging...");
        this.playercount = new HBox(unknownLabel);
        this.motd = new Label();

        updateMotd();

        name.getStyleClass().add("server-name");
        ping.getStyleClass().add("server-ping");
        unknownLabel.getStyleClass().add("player-separator");

        this.header = buildHeader();

        Image rawIcon;
        @Nullable byte[] favicon = server.getFavicon();
        if (favicon != null) {
            rawIcon = ResourceUtil.imageFromByteArray(favicon);
        } else {
            rawIcon = new Image(ResourceUtil.loadResource("/icons/unknown_server.png").toExternalForm());
        }
        this.icon = new ImageView(rawIcon);
        this.icon.setFitWidth(64);
        this.icon.setFitHeight(64);
        this.icon.setPreserveRatio(false);
        BorderPane.setMargin(icon, new Insets(0, 5, 0, 0));

        setCenter(buildCentre());
        setLeft(icon);

        pingTask.submit(() -> {
            Pinger.ping(server)
                    .thenAccept(unused -> Platform.runLater(this::updateComponents));
        });
    }

    private VBox buildCentre() {
        VBox centre = new VBox(5);
        centre.getChildren().addAll(header, motd);
        return centre;
    }

    private GridPane buildHeader() {
        GridPane header = new GridPane();

        header.setHgap(10);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.ALWAYS);
        column1.setFillWidth(true);

        ColumnConstraints column2 = new ColumnConstraints();
        ColumnConstraints column3 = new ColumnConstraints();

        header.getColumnConstraints().addAll(column1, column2, column3);

        name.setAlignment(Pos.CENTER);
        header.add(name, 0, 0);
        header.add(playercount, 1, 0);
        header.add(ping, 2, 0);

        return header;
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
        motd.getStyleClass().clear();

        Component descriptionComp = server.getDescription();

        if (descriptionComp == null) {
            if (server.getStatus() == ServerInfo.Status.PINGING || server.getStatus() == ServerInfo.Status.INITIAL) {
                motd.setText("Pinging...");
                motd.getStyleClass().add("server-pinging");
            } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
                motd.setText("Cannot connect to server");
                motd.getStyleClass().add("pinger-error");
            }
        } else {
            motd.setText(miniMessage.serialize(server.getDescription()));
            motd.getStyleClass().add("server-motd");
        }
    }

    private void updateComponents() {
        updatePlayerCount();

        if (server.getStatus() == ServerInfo.Status.SUCCESSFUL) {
            ping.setText(server.getPing() + " ms");
        } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
            ping.setText("Can't connect");
        } else {
            ping.setText("Pinging...");
        }

        if (server.getFavicon() != null) {
            icon.setImage(ResourceUtil.imageFromByteArray(server.getFavicon()));
        }

        updateMotd();
    }
}
