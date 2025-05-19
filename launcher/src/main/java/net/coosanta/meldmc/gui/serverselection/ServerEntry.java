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
        this.server = server;

        this.name = new Label(server.getName());
        this.ping = new Label("Pinging...");
        this.playercount = new HBox(new Label("unknown"));
        this.motd = new Label(formatDescription()); // TODO: Grey unless otherwise stated

        name.getStyleClass().add("server-name");
        ping.getStyleClass().add("server-ping");
        playercount.getStyleClass().add("server-playercount");
        motd.getStyleClass().add("server-motd");

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

        setTop(header);
        setCenter(motd);
        setLeft(icon);

        pingTask.submit(() -> {
            Pinger.ping(server)
                    .thenAccept(unused -> Platform.runLater(this::updateComponents));
        });
    }

    private GridPane buildHeader() {
        GridPane header = new GridPane();

        header.setPadding(new Insets(2, 5, 2, 5));

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
            playercount.getChildren().add(new Label("unknown"));
        } else {
            playercount.getChildren().addAll(
                    new Label(String.valueOf(serverPlayers.getOnlinePlayers())),
                    new Label("/"), // TODO: Grey
                    new Label(String.valueOf(serverPlayers.getMaxPlayers())));
        }
    }

    private String formatDescription() {
        Component descriptionComp = server.getDescription();
        String description = "";
        if (descriptionComp == null) {
            if (server.getStatus() == ServerInfo.Status.PINGING || server.getStatus() == ServerInfo.Status.INITIAL) {
                description = "Pinging..."; // TODO: Grey
            } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
                description = "Cannot connect to server"; // TODO: Red
            }
        } else {
            description = miniMessage.serialize(server.getDescription());
        }
        return description;
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

        motd.setText(formatDescription());
    }
}
