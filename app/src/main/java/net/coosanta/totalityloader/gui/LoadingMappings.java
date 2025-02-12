package net.coosanta.totalityloader.gui;

import com.mojang.logging.LogUtils;
import net.coosanta.totalityloader.minecraft.MinecraftClasses;
import org.slf4j.Logger;

import javax.swing.*;
import java.io.IOException;

public class LoadingMappings extends JPanel {
    private static Logger LOGGER = LogUtils.getLogger();
    public LoadingMappings(JFrame parent) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(CENTER_ALIGNMENT);

        JTextArea information = new JTextArea("Loading Minecraft Mappings");
        JProgressBar progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setIndeterminate(true);

        add(information);
        add(progressBar);

        if (MinecraftClasses.isInitiated()) throw new RuntimeException("It already exists wtf!");

        LOGGER.info("Loading Progress listener initiated.");

        LoadingProgressListener loadingProgress = new LoadingProgressListener() {
            @Override
            public void onProcessingMappings(int max) {
                LOGGER.info("Processing mappings from yarn tiny file");
                SwingUtilities.invokeLater(() -> {
                    progressBar.setMaximum(max);
                    progressBar.setIndeterminate(false);
                });
            }

            @Override
            public void onProgressUpdate(int progress) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(progress);
                    progressBar.setString(progress + "/" + progressBar.getMaximum());
                });
            }

            @Override
            public void onComplete() {
                LOGGER.info("Completed and closed loading progress listener");
                SwingUtilities.invokeLater(() -> {
                    try {
                        parent.setContentPane(new ServerSelect());
                        parent.revalidate();
                        parent.repaint();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        };
        new Thread(() -> {
            try {
                MinecraftClasses.getInstance().initiate(loadingProgress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public interface LoadingProgressListener {
        void onProcessingMappings(int max);
        void onProgressUpdate(int progress);
        void onComplete();
    }
}
