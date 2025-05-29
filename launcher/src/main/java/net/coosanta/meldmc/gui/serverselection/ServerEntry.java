package net.coosanta.meldmc.gui.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.network.Pinger;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ServerEntry extends BorderPane {
    private final Label unknownLabel = new Label("unknown");
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ServerInfo server;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final ImageView icon;
    private final Label name;
    private final Label ping;
    private final HBox playercount;
    private final GridPane header;

    private final TextFlow motdFlow;
    private static final Map<Character, String> FORMATTING_CODES = Map.ofEntries(
            Map.entry('0', "mc-black"),
            Map.entry('1', "mc-dark-blue"),
            Map.entry('2', "mc-dark-green"),
            Map.entry('3', "mc-dark-aqua"),
            Map.entry('4', "mc-dark-red"),
            Map.entry('5', "mc-dark-purple"),
            Map.entry('6', "mc-gold"),
            Map.entry('7', "mc-gray"),
            Map.entry('8', "mc-dark-gray"),
            Map.entry('9', "mc-blue"),
            Map.entry('a', "mc-green"),
            Map.entry('b', "mc-aqua"),
            Map.entry('c', "mc-red"),
            Map.entry('d', "mc-light-purple"),
            Map.entry('e', "mc-yellow"),
            Map.entry('f', "mc-white"),
            Map.entry('l', "mc-bold"),
            Map.entry('m', "mc-strikethrough"),
            Map.entry('n', "mc-underline"),
            Map.entry('o', "mc-italic")
    );

    public ServerEntry(ServerInfo server, ExecutorService pingTask) {
        setPadding(new Insets(5, 10, 5, 10));

        this.server = server;

        this.name = new Label(server.getName());
        this.ping = new Label("Pinging...");
        this.playercount = new HBox(unknownLabel);

        this.motdFlow = new TextFlow();
        this.motdFlow.getStyleClass().add("server-motd");

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
            try {
                rawIcon = new Image(ResourceUtil.loadResource("/icons/unknown_server.png").toExternalForm());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.icon = new ImageView(rawIcon);
        this.icon.setFitWidth(64);
        this.icon.setFitHeight(64);
        this.icon.setPreserveRatio(false);
        BorderPane.setMargin(icon, new Insets(0, 5, 0, 0));

        setCenter(buildCentre());
        setLeft(icon);

        pingTask.submit(() -> Pinger.ping(server, this));
    }

    private VBox buildCentre() {
        VBox centre = new VBox(5);
        centre.getChildren().addAll(header, motdFlow);
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
        motdFlow.getStyleClass().clear();
        motdFlow.getChildren().clear();

        Component descriptionComp = server.getDescription();

        if (descriptionComp == null) {
            if (server.getStatus() == ServerInfo.Status.PINGING || server.getStatus() == ServerInfo.Status.INITIAL) {
                Text text = new Text("Pinging...");
                text.getStyleClass().add("server-motd-pinging");
                motdFlow.getChildren().add(text);
            } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
                Text text = new Text("Cannot connect to server");
                text.getStyleClass().add("pinger-motd-ping-error");
                motdFlow.getChildren().add(text);
            }
        } else {
            // TODO: Modern minecraft styling.
            String motdString = miniMessage.serialize(server.getDescription());
            parseFormattedText(motdString);
        }
    }

    private void parseFormattedText(String text) {
        StringBuilder currentText = new StringBuilder();
        Map<String, Boolean> activeClasses = new HashMap<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                addTextNode(currentText, activeClasses);

                char formatCode = text.charAt(++i);
                if (formatCode == 'r') {
                    activeClasses.clear();
                } else if (FORMATTING_CODES.containsKey(formatCode)) {
                    String cssClass = FORMATTING_CODES.get(formatCode);
                    activeClasses.put(cssClass, true);
                } // TODO: explore default minecraft behaviour with invalid formatting codes
            } else {
                currentText.append(c);
            }
        }

        addTextNode(currentText, activeClasses);
    }

    private void addTextNode(StringBuilder currentText, Map<String, Boolean> activeClasses) {
        if (currentText.isEmpty()) return;

        Text textNode = new Text(currentText.toString());
        textNode.getStyleClass().add("server-motd");

        for (Map.Entry<String, Boolean> entry : activeClasses.entrySet()) {
            if (entry.getValue()) {
                textNode.getStyleClass().add(entry.getKey());
            }
        }

        motdFlow.getChildren().add(textNode);
        currentText.setLength(0);
    }

    public void updateComponents() {
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
