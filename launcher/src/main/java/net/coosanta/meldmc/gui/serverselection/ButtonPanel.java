package net.coosanta.meldmc.gui.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.coosanta.meldmc.gui.button.MinecraftButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ButtonPanel extends GridPane {
    private static final Logger log = LoggerFactory.getLogger(ButtonPanel.class);

    private final MinecraftButton joinServerButton;
    private final MinecraftButton serverInfoButton;
    private final MinecraftButton addServerButton;
    private final MinecraftButton editButton;
    private final MinecraftButton deleteButton;
    private final MinecraftButton refreshButton;
    private final MinecraftButton settingsButton;

    public ButtonPanel() {
        configureGrid();

        joinServerButton = new MinecraftButton("Join Server", true);
        serverInfoButton = new MinecraftButton("Server Info", true);
        addServerButton = new MinecraftButton("Add Server");

        editButton = new MinecraftButton("Edit", true);
        deleteButton = new MinecraftButton("Delete", true);
        refreshButton = new MinecraftButton("Refresh");
        settingsButton = new MinecraftButton("Settings");

        addButtons(List.of(joinServerButton, serverInfoButton, addServerButton), 0, 4);
        addButtons(List.of(editButton, deleteButton, refreshButton, settingsButton), 1, 3);

        setupEventHandlers();
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

    private void configureGrid() {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(15));
        setAlignment(Pos.CENTER);

        for (int i = 0; i < 12; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setHgrow(Priority.ALWAYS);
            column.setPercentWidth(100.0 / 12);
            getColumnConstraints().add(column);
        }
    }

    private void addButtons(List<MinecraftButton> buttons, int row, int colspan) {
        for (int i = 0; i < buttons.size(); i++) {
            MinecraftButton button = buttons.get(i);
            button.setPrefHeight(50);
            GridPane.setFillWidth(button, true);
            add(button, i * colspan, row, colspan, 1);
        }
    }

    private void handleJoinServer() {
        log.debug("Join server clicked");
    }

    private void handleServerInfo() {
        log.debug("Server info clicked");
    }

    private void handleAddServer() {
        log.debug("Add server clicked");
    }

    private void handleEditServer() {
        log.debug("Edit clicked");
    }

    private void handleDeleteServer() {
        log.debug("Delete clicked");
    }

    private void handleRefresh() {
        log.debug("Refresh clicked");
    }

    private void handleSettings() {
        log.debug("Settings clicked");
    }
}