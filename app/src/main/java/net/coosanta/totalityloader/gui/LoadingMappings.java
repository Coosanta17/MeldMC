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

        try {
            MinecraftClasses.initiate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                parent.setContentPane(new ServerSelect());
                parent.revalidate();
                parent.repaint();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        LOGGER.info("Closed LoadingMappings JPanel");
    }
}
