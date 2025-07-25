package net.coosanta.meldmc.gui.controllers.meldserverinfo;

import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.utility.ResourceUtil;

public class ModEntry extends BorderPane {
    private final MeldData.ClientMod modData;

    private final Label name;
    private final Label version;

    public ModEntry(MeldData.ClientMod data) {
        this.name = new Label(data.modname());
        this.version = new Label(data.modVersion());

        getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        name.getStyleClass().add("mod-name");
        version.getStyleClass().add("mod-version");

        setTop(name);
        setBottom(version);

        this.modData = data;
    }

    public MeldData.ClientMod getModData() {
        return modData;
    }
}
