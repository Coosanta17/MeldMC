package net.coosanta.meldmc.gui.controllers.joinserver;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;
import net.coosanta.meldmc.minecraft.GameInstance;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static net.coosanta.meldmc.Main.DESIGN_WIDTH;
import static net.coosanta.meldmc.gui.controllers.joinserver.LaunchConfigurer.configureAndLaunch;

public class ModDownloadConfirmation extends BorderPane {
    private static final Logger log = LoggerFactory.getLogger(ModDownloadConfirmation.class);
    private final GameInstance serverInstance;

    @FXML
    private Label warning;

    @FXML
    private TitledPane modrinthTitle;
    @FXML
    private TitledPane serverSentTitle;
    @FXML
    private TitledPane untrustedTitle;
    @FXML
    private TitledPane deletedTitle;

    @FXML
    private VBox newModrinthMods;
    @FXML
    private VBox newServerMods;
    @FXML
    private VBox newUntrustedMods;
    @FXML
    private VBox oldDeletedMods;

    @FXML
    private MinecraftButton confirm;
    @FXML
    private MinecraftButton cancel;

    public ModDownloadConfirmation(@NotNull GameInstance serverInstance) {
        this.serverInstance = serverInstance;

        setPrefWidth(DESIGN_WIDTH);
        setMaxWidth(DESIGN_WIDTH);

        try {
            ResourceUtil.loadFXML("/fxml/joinserver/ModDownloadConfirmation.fxml", this).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cancel.setOnAction(this::cancelClicked);
        confirm.setOnAction(ð -> configureAndLaunch(serverInstance));

        warning.setText(buildWarningMessage(serverInstance.getAddress()));

        serverInstance.getChangedMods().values().forEach(mod -> {
            switch (mod.modSource()) {
                case MODRINTH -> newModrinthMods.getChildren().add(new ModSummary(mod));
                case CURSEFORGE -> throw new IllegalArgumentException("CurseForge not supported (yet)");
                case SERVER -> newServerMods.getChildren().add(new ModSummary(mod));
                case UNTRUSTED -> newUntrustedMods.getChildren().add(new ModSummary(mod));
            }
        });
        serverInstance.getDeletedMods().values().forEach(modPath -> {
            String modHash;
            try {
                modHash = ResourceUtil.calculateSHA512(modPath.toFile());
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to calculate hash of deleted mod at path " + modPath, e);
            }
            var mod = serverInstance.getCachedMeldData().modMap().get(modHash); // FIXME possible nullptr ex on non-meld or cached servers
            if (mod != null) {
                oldDeletedMods.getChildren().add(new ModSummary(mod));
            } else {
                var modLabel = new Label(modPath.getFileName().toString());
                modLabel.getStyleClass().add("black");
                oldDeletedMods.getChildren().add(modLabel);
            }
        });

        modifyNodeVisibility(modrinthTitle, !newModrinthMods.getChildren().isEmpty());
        modifyNodeVisibility(serverSentTitle, !newServerMods.getChildren().isEmpty());
        modifyNodeVisibility(untrustedTitle, !newUntrustedMods.getChildren().isEmpty());
        modifyNodeVisibility(deletedTitle, !oldDeletedMods.getChildren().isEmpty());
    }

    private @NotNull String buildWarningMessage(String address) {
        assert serverInstance != null;
        int changed = serverInstance.getChangedMods().size();
        return """
                You are about to download and run %d new or updated mod%s requested by the Minecraft server at address '%s'.
                
                Mods are executable code. They can access your files, network, and other software, and can cause serious damage if malicious.
                
                Proceed only if you trust the server operator.
                
                Be especially cautious about mods automatically sent by the server or from links other than Modrinth or CurseForge.
                
                MeldMC and Mojang do not review or endorse these mods and are not liable for any damage.
                """.formatted(changed, changed == 1 ? "" : "s", address);
    }

    private void modifyNodeVisibility(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void cancelClicked(ActionEvent event) {
        log.debug("Cancel server clicked");
// tODO figure out what to do with the cancel button - where put it?
//        MainWindow.getInstance().getController().showMeldInfoPanel();
    }
}
