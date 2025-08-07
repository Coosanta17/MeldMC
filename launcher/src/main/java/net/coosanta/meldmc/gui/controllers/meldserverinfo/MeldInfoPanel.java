package net.coosanta.meldmc.gui.controllers.meldserverinfo;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.gui.views.MainWindow;
import net.coosanta.meldmc.minecraft.InstanceManager;
import net.coosanta.meldmc.minecraft.ServerInfo;
import net.coosanta.meldmc.network.client.MeldData;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeldInfoPanel extends BorderPane implements ScaleFactorCssProperty.ScaleFactorContainer {
    private static final Logger log = LoggerFactory.getLogger(MeldInfoPanel.class);
    private final ScaleFactorCssProperty scaleFactorProperty;

    @FXML
    private Label header;

    @FXML
    private ScrollPane leftScrollpane;
    @FXML
    private StackPane leftContainer;
    @FXML
    private VBox leftPanel;
    @FXML
    private VBox modsPanel;

    @FXML
    private ScrollPane centreScrollPane;
    @FXML
    private Text mcVersion;
    @FXML
    private Text modLoader;
    @FXML
    private Text modLoaderVersionLabel;
    @FXML
    private Text modLoaderVersion;

    @FXML
    TextField modSearch;
    @FXML
    MinecraftButton joinServer;
    @FXML
    MinecraftButton openInstanceFolder;
    @FXML
    MinecraftButton done;

    List<ModEntry> serverMods = new ArrayList<>();
    @FXML
    private ModEntryInfo modInfo;

    private final MeldData meldData;
    private ModEntry selectedMod = null;

    public MeldInfoPanel(ServerInfo server) {
        try {
            ResourceUtil.loadFXML("/fxml/meldserverinfo/MeldInfoPanel.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML for ServerEntry", e);
        }

        this.meldData = server.getMeldData();
        if (this.meldData == null) {
            throw new IllegalStateException("No MeldData available for server " + server.getName());
        }

        header.setText(server.getName());

        mcVersion.setText(meldData.mcVersion());
        modLoader.setText(switch (meldData.modLoader()) {
            case VANILLA -> "Vanilla";
            case FORGE -> "Forge";
            case NEOFORGE -> "NeoForge";
            case FABRIC -> "Fabric";
            case QUILT -> "Quilt";
            case null -> "Unknown";
        });

        modLoaderVersionLabel.setText(modLoader.getText() + " Version: ");
        modLoaderVersion.setText(meldData.modLoaderVersion());

        this.scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");

        server.getMeldData().modMap().forEach((key, value) -> {
            if (!key.equals(value.hash())) {
                log.error("Mod with id '{}' has mismatching hashes sent from server!", value.modId());
                return;
            }

            ModEntry modEntry = new ModEntry(value);

            modEntry.setOnMouseClicked(event -> selectMod(modEntry));

            serverMods.add(modEntry);
        });

        modsPanel.getChildren().addAll(serverMods);

        openInstanceFolder.setOnAction(event -> {
            try {
                if (!Desktop.isDesktopSupported()) throw new IOException("Java desktop API is not supported on this machine.");
                Desktop.getDesktop().open(InstanceManager.getInstance(server.getAddress()).getInstanceDir().toFile());
            } catch (IOException e) {
                log.error("Failed to open instance folder for server address: {}", server.getAddress(), e);
            }
        });

        joinServer.setOnAction(e -> {
            log.info("akdjfhsdkfjchhkewjfhkdsajhdkasdjhksdfjhaksdfjdhskf (Join server pressed)");
            // TODO JOIN THE FUCKING SERVER
        });
        done.setOnAction(e -> MainWindow.getInstance().getController().showSelectionPanel());
        modSearch.textProperty().addListener((observable, oldValue, newValue) -> searchMod(newValue));

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                leftScrollpane.setPrefWidth(stage.getWidth() / 3.0);
                leftScrollpane.setMaxWidth(stage.getWidth() / 3.0);
                stage.widthProperty().addListener((o, oldWidth, newWidth) -> {
                    leftScrollpane.setPrefWidth(newWidth.doubleValue() / 3.0);
                    leftScrollpane.setMaxWidth(newWidth.doubleValue() / 3.0);
                });
            }
        });
    }

    private void selectMod(ModEntry newSelection) {
        if (selectedMod != null) {
            selectedMod.getStyleClass().remove("entry-selected");
        }

        if (newSelection != null) {
            modInfo.updateServer(newSelection.getModData());
            newSelection.getStyleClass().add("entry-selected");
        }

        selectedMod = newSelection;
    }

    private void searchMod(String term) {
        modsPanel.getChildren().clear();
        if (term == null || term.isBlank()) {
            modsPanel.getChildren().addAll(serverMods);
            return;
        }
        String lowerTerm = term.toLowerCase();
        for (ModEntry entry : serverMods) {
            if (entry.getModData().modname().toLowerCase().contains(lowerTerm)) {
                modsPanel.getChildren().add(entry);
            }
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
