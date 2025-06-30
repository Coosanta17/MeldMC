package net.coosanta.meldmc.gui.controllers;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.minecraft.ServerListManager;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.coosanta.meldmc.Main.*;

public class EditServer extends BorderPane implements ScaleFactorCssProperty.ScaleFactorContainer {
    private static final Logger log = LoggerFactory.getLogger(EditServer.class);
    private final ScaleFactorCssProperty scaleFactorProperty;
    private final @Nullable Integer index;
    private final ServerInfo server;

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
    private MinecraftButton done;
    @FXML
    private MinecraftButton cancel;

    public EditServer(@Nullable Integer index) {
        setPrefSize(DESIGN_WIDTH * 0.667, DESIGN_HEIGHT);
        setMaxSize(DESIGN_WIDTH * 0.667, DESIGN_HEIGHT);

        scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");

        try {
            ResourceUtil.loadFXML("/fxml/EditServer.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for EditServer", e);
        }

        if (index != null) {
            this.server = ServerListManager.getInstance().getServers().get(index);

            serverNameInput.setText(server.getName());
            serverAddressInput.setText(server.getAddress());
        } else {
            this.server = new ServerInfo(null, null);
        }

        serverNameInput.textProperty().addListener((observable, oldValue, newValue)
                -> updateDoneButtonState());

        serverAddressInput.textProperty().addListener((observable, oldValue, newValue)
                -> updateDoneButtonState());

        cancel.setOnAction(e -> MainWindow.getInstance().getController().showSelectionPanel());
        done.setOnAction(e -> handleDone());
        updateDoneButtonState();

        this.index = index;
    }

    private void updateDoneButtonState() {
        done.setDisable(serverNameInput.getText().isEmpty() || serverAddressInput.getText().isEmpty());
    }

    private void handleDone() {
        ServerListManager serverListManager = ServerListManager.getInstance();
        log.debug("Done button pressed");
        if (index != null) {
            server.setName(serverNameInput.getText());
            server.setAddress(serverAddressInput.getText());
            serverListManager.updateServer(index, server);
        } else {
            serverListManager.addServer(new ServerInfo(serverNameInput.getText(), serverAddressInput.getText()));
        }
        MainWindowController mainWindowController = MainWindow.getInstance().getController();

        mainWindowController.getSelectionPanel().getCentrePanel().reload();
        mainWindowController.getSelectionPanel().selectEntry(null, null);

        mainWindowController.showSelectionPanel();
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
