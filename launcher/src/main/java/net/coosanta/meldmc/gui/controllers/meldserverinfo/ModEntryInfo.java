package net.coosanta.meldmc.gui.controllers.meldserverinfo;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class ModEntryInfo extends VBox {
    private static final Logger log = LoggerFactory.getLogger(ModEntryInfo.class);
    @FXML
    private Text modname;
    @FXML
    private Text version;
    @FXML
    private Text versionLabel;
    @FXML
    private TextField hash;
    @FXML
    private Text hashLabel;
    @FXML
    private Text filename;
    @FXML
    private Text filenameLabel;
    @FXML
    private Text fileSize;
    @FXML
    private Text fileSizeLabel;
    @FXML
    private Text urlLabel;
    @FXML
    private Text modId;
    @FXML
    private Text modIdLabel;
    @FXML
    private Text authors;
    @FXML
    private Text authorsLabel;
    @FXML
    private Text description;
    @FXML
    private HBox urlFlow;
    @FXML
    private TextField url;
    @FXML
    private TextFlow viewProjectFlow;
    @FXML
    private Text viewProject;

    public ModEntryInfo() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            ResourceUtil.loadFXML("/fxml/meldserverinfo/ModEntryInfo.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for ModEntryInfo", e);
        }
    }

    public void updateServer(MeldData.ClientMod clientMod) {
        authorsLabel.setVisible(true);
        filenameLabel.setVisible(true);
        fileSizeLabel.setVisible(true);
        modIdLabel.setVisible(true);
        hashLabel.setVisible(true);
        versionLabel.setVisible(true);

        modname.setText(clientMod.modname());
        version.setText(clientMod.modVersion());
        hash.setText(clientMod.hash());
        filename.setText(clientMod.filename());
        if (clientMod.fileSize() >= 1e+6) {
            fileSize.setText(String.format("%.2f MB", clientMod.fileSize() / 1e+6));
        } else {
            fileSize.setText(String.format("%.2f KB", clientMod.fileSize() / 1000.0));
        }
        if (clientMod.url() == null) {
            urlFlow.setVisible(false);
        } else {
            url.setText(clientMod.url());
            urlFlow.setVisible(true);
        }
        if (clientMod.projectUrl() == null) {
            viewProjectFlow.setVisible(false);
        } else {
            String modSource = switch (clientMod.modSource()) {
                case MODRINTH -> "Modrinth";
                case CURSEFORGE -> "CurseForge";
                case UNTRUSTED -> "Untrusted source: " + clientMod.projectUrl();
                case SERVER -> throw new IllegalStateException("Mod with a project URL should not be from the server");
            };
            viewProject.setText("View project on " + modSource);
            viewProject.setOnMouseClicked(event -> {
                try {
                    Desktop.getDesktop().browse(new URI(clientMod.projectUrl()));
                } catch (Exception ex) {
                    log.error("Failed to open hyperlink.", ex);
                }
            });
            viewProjectFlow.setVisible(true);
        }
        modId.setText(clientMod.modId());
        authors.setText(clientMod.authors());
        description.setText(clientMod.description());
    }
}
