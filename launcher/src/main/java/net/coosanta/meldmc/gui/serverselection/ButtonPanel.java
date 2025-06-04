package net.coosanta.meldmc.gui.serverselection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import net.coosanta.meldmc.gui.button.MinecraftButton;

public class ButtonPanel extends GridPane {
    public ButtonPanel() {
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

        for (int i = 0; i < 3; i++) {
            MinecraftButton button = new MinecraftButton("Button " + (i + 1));
            button.setPrefHeight(60);
            button.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(button, true);
            add(button, i * 4, 0, 4, 1);
        }

        for (int i = 0; i < 4; i++) {
            MinecraftButton button = new MinecraftButton("Button " + (i + 4));
            button.setPrefHeight(45);
            button.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(button, true);
            add(button, i * 3, 1, 3, 1);
        }
    }
}
