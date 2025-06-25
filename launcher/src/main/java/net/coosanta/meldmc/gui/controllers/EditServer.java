package net.coosanta.meldmc.gui.controllers;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.coosanta.meldmc.Main.DESIGN_HEIGHT;
import static net.coosanta.meldmc.Main.DESIGN_WIDTH;

public class EditServer extends BorderPane implements ScaleFactorCssProperty.ScaleFactorContainer {
    private final ScaleFactorCssProperty scaleFactorProperty;

    @FXML
    private Label header;
    @FXML
    private Label serverNameLabel;
    @FXML
    private TextField serverNameInput;
    @FXML
    private Label serverAddressLabel;
    @FXML
    private TextField serverAddressInput;
    @FXML
    private MinecraftButton serverResourcePacks;
    @FXML
    private MinecraftButton done;
    @FXML
    private MinecraftButton cancel;

    public EditServer() {
        setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        setMaxSize(DESIGN_WIDTH,DESIGN_HEIGHT);

        scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");

        try {
            ResourceUtil.loadFXML("/fxml/EditServer.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for EditServer", e);
        }
    }

    @Override
    public StyleableProperty<Number> getScaleFactorProperty() {
        return scaleFactorProperty.property();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(BorderPane.getClassCssMetaData());
        styleables.add(ScaleFactorCssProperty.getCssMetaData());
        return Collections.unmodifiableList(styleables);
    }
}
