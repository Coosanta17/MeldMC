package net.coosanta.meldmc.gui.controllers;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import net.coosanta.meldmc.exceptions.ClientJsonNotFoundException;
import net.coosanta.meldmc.gui.views.MainWindow;

import java.awt.Desktop;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

public class ExceptionGUI extends StackPane {
    public ExceptionGUI(Throwable exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setFont(Font.font("Monospaced"));
        textArea.setStyle("-fx-control-inner-background: white; -fx-text-fill: black;");
        textArea.setPrefRowCount(20);
        textArea.setPrefColumnCount(60);

        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textArea.getText());
            clipboard.setContent(content);
        });

        VBox vbox = new VBox(10, textArea, copyButton);
        vbox.setStyle("-fx-padding: 10;");

        if (exception instanceof ClientJsonNotFoundException cj && cj.isForge()) {
            Button downloadForge = new Button("Download Forge");
            downloadForge.setOnAction(evt -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html"));
                } catch (Exception e) {
                    Platform.runLater(() -> MainWindow.getInstance().getController().showExceptionScreen(e)); // lmao

                }
            });
            vbox.getChildren().add(downloadForge);
        }

        getChildren().add(vbox);
    }
}
