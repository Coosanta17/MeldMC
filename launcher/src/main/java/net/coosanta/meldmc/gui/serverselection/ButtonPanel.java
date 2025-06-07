package net.coosanta.meldmc.gui.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.coosanta.meldmc.gui.button.MinecraftButton;

import java.util.List;

public class ButtonPanel extends GridPane {
    public ButtonPanel() {
        configureGrid();
        addButtons(List.of(
                new MinecraftButton("Join Server", true),
                new MinecraftButton("Server Info", true),
                new MinecraftButton("Add Server")
        ), 0, 4);

        addButtons(List.of(
                new MinecraftButton("Edit", true),
                new MinecraftButton("Delete", true),
                new MinecraftButton("Refresh"),
                new MinecraftButton("Settings")
        ), 1, 3);
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
}