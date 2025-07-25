package net.coosanta.meldmc.gui.controllers.meldserverinfo;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.utility.ResourceUtil;

import java.io.IOException;

public class ModEntryInfo extends VBox {
    @FXML
    private Text modname;
    @FXML
    private Text version;
    @FXML
    private Text versionLabel;
    @FXML
    private Text hash;
    @FXML
    private Text hashLabel;
    @FXML
    private Text filename;
    @FXML
    private Text filenameLabel;
    @FXML
    private TextFlow urlFlow;
    @FXML
    private Text url;
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
        modIdLabel.setVisible(true);
        hashLabel.setVisible(true);
        versionLabel.setVisible(true);

        modname.setText(clientMod.modname());
        version.setText(clientMod.modVersion());
        hash.setText(clientMod.hash());
        filename.setText(clientMod.filename());
        if (clientMod.url() == null) {
            urlFlow.setVisible(false);
        } else {
            urlFlow.setVisible(true);
            url.setText(clientMod.url());
        }
        modId.setText(clientMod.modId());
        authors.setText(clientMod.authors());
        description.setText(clientMod.description());
    }
}
