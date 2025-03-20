package net.coosanta.totalityloader.gui.lookandfeel;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class MinecraftPanel extends JPanel implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(MinecraftPanel.class);

    private JPanel backgroundPanel;
    private JPanel foregroundPanel;

    private final int designWidth = 800;
    private final int designHeight = 600;
    private double scaleFactor = 1.0;

    public MinecraftPanel(JPanel foregroundPanel) {
        super();
        setOpaque(false);
        setLayout(new OverlayLayout(this));

        this.foregroundPanel = foregroundPanel;

        backgroundPanel = new BackgroundPanel();

        add(foregroundPanel);
        add(backgroundPanel);
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

        // Apply scaling to child components if needed
        if (backgroundPanel instanceof ScalablePanel) {
            ((ScalablePanel) backgroundPanel).applyScale(scaleFactor);
        }

        if (foregroundPanel instanceof ScalablePanel) {
            ((ScalablePanel) foregroundPanel).applyScale(scaleFactor);
        }

        repaint();
    }

    public JPanel getBackgroundPanel() {
        return backgroundPanel;
    }

    public JPanel getForegroundPanel() {
        return foregroundPanel;
    }

    private class BackgroundPanel extends JPanel implements ScalablePanel {
        private Image background;

        private final double scaleFactorFactor = 3.0;
        private double scaleFactorModified = scaleFactor * scaleFactorFactor;

        private BackgroundPanel() {
            try {
                background = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icons/background.png")));
            } catch (IOException e) {
                log.error("Cannot load background image background.png.\n{}", String.valueOf(e));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                int width = (int) (background.getWidth(this) * scaleFactorModified);
                int height = (int) (background.getHeight(this) * scaleFactorModified);
                for (int x = 0; x < getWidth(); x += width) {
                    for (int y = 0; y < getHeight(); y += height) {
                        g2d.drawImage(background, x, y, width, height, this);
                    }
                }
                g2d.dispose();
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
            MinecraftPanel.this.scaleFactor = scaleFactor;
            this.scaleFactorModified = scaleFactor * scaleFactorFactor;
            refreshGui(this);
        }
    }
}
