package net.coosanta.totalityloader.gui.lookandfeel;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class MinecraftPanel extends JPanel implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(MinecraftPanel.class);
    private Image background;

    private final int designWidth = 800;
    private final int designHeight = 600;
    private double scaleFactor = 1.0;

    public MinecraftPanel() {
        super();
        setOpaque(false);
        try {
            background = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icons/dirt.png")));
        } catch (IOException e) {
            log.error("Cannot load background image dirt.png.\n{}", String.valueOf(e));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Tile image with scaling
        if (background != null) {
            int scaledImgWidth = (int)(background.getWidth(this) * scaleFactor);
            int scaledImgHeight = (int)(background.getHeight(this) * scaleFactor);

            if (scaledImgWidth > 0 && scaledImgHeight > 0) {
                for (int x = 0; x < getWidth(); x += scaledImgWidth) {
                    for (int y = 0; y < getHeight(); y += scaledImgHeight) {
                        g.drawImage(background, x, y, scaledImgWidth, scaledImgHeight, this);
                    }
                }
            }
        }
    }

    @Override
    public double getDesignWidth() {
        return designWidth;
    }

    @Override
    public double getDesignHeight() {
        return designHeight;
    }

    @Override
    public void applyScale(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        repaint();
    }
}
