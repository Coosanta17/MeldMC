package net.coosanta.meldmc.gui.controllers.meldserverinfo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.utility.ResourceUtil;

import java.io.IOException;

public class ModEntry extends HBox {
    private final MeldData.ClientMod modData;

    @FXML
    private Label name;
    @FXML
    private Label version;
    @FXML
    private ImageView sourceIcon;

    public ModEntry(MeldData.ClientMod data) {
        try {
            ResourceUtil.loadFXML("/fxml/meldserverinfo/ModEntry.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.name.setText(data.modname());
        this.version.setText(data.modVersion());

        if (data.modSource() == MeldData.ClientMod.ModSource.SERVER) {
            this.sourceIcon.setImage(ResourceUtil.getImage("/icons/mod_distributors/server.png"));
        } else if (data.modSource() == MeldData.ClientMod.ModSource.MODRINTH) {
            this.sourceIcon.setImage(ResourceUtil.getImage("/icons/mod_distributors/modrinth.png"));
        } else {
            this.sourceIcon.setImage(ResourceUtil.getImage("/icons/mod_distributors/warn.png"));
        }

        this.modData = data;
    }

    public MeldData.ClientMod getModData() {
        return modData;
    }
}
