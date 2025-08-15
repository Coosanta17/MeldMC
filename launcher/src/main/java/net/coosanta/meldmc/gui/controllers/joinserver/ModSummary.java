package net.coosanta.meldmc.gui.controllers.joinserver;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import net.coosanta.meldmc.network.client.MeldData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.net.URI;

public class ModSummary extends VBox {
    private static final Logger log = LoggerFactory.getLogger(ModSummary.class);

    public ModSummary(MeldData.ClientMod mod) {
        Text filename = new Text(mod.filename());
        filename.getStyleClass().add("black");

        Text filesize = new Text(mod.fileSize() > 1_000_000 ? (mod.fileSize() / 1_000_000) + "MB" : (mod.fileSize() / 1000) + "KB");
        filesize.getStyleClass().add("file-size");

        getChildren().addAll(filename, filesize, buildContext(mod));
    }

    private @NotNull Node buildContext(MeldData.ClientMod mod) {
        return switch (mod.modSource()) {
            case MODRINTH -> {
                Text modContext = new Text("View project");
                modContext.getStyleClass().add("url-hyperlink");
                modContext.setOnMouseClicked(e -> openUrl(mod.projectUrl()));
                yield modContext;
            }
            case SERVER -> {
                Text modContext = new Text("Sent from server");
                modContext.getStyleClass().add("mod-info-field-label");
                yield modContext;
            }
            case UNTRUSTED -> {
                TextField modContext = new TextField(mod.url());
                modContext.getStyleClass().add("selectable-textfield-black");
                yield modContext;
            }
            case CURSEFORGE -> throw new IllegalArgumentException("CurseForge not supported yet.");
        };
    }

    private void openUrl(String url) {
        try {
            if (url != null) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            log.error("Failed to open hyperlink.", ex);
        }
    }
}
