package net.coosanta.meldmc.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.coosanta.meldmc.Main;
import net.coosanta.meldmc.utility.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainWindow extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    private static final java.awt.Dimension windowDimension = Main.getWindowsSize();

    @Override
    public void start(Stage stage) throws IOException {
        log.info("Starting application main window");

        FXMLLoader loader = new FXMLLoader(ResourceUtil.loadResource("/fxml/MainWindow.fxml"));
        StackPane root = loader.load();

        MainWindowController controller = loader.getController();

        Scene scene = new Scene(root, windowDimension.width, windowDimension.height);
        scene.getStylesheets().add(ResourceUtil.loadResource("/styles/base-style.css").toExternalForm());

        stage.setScene(scene);

        controller.setStage(stage);

        stage.show();
    }
}
