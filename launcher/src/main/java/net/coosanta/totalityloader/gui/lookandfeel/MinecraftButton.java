package net.coosanta.totalityloader.gui.lookandfeel;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MinecraftButton extends JButton implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(MinecraftButton.class);
    private final Dimension designSize;
    private double currentScale = 1.0;

    private BufferedImage normalIcon;
    private BufferedImage hoverIcon;
    private BufferedImage currentIcon;

    // Size of the left and right borders of the button in pixels.
    private static final int BORDER_SIZE = 2;

    public MinecraftButton(String text, Dimension designSize) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        this.designSize = designSize;

        try {
            normalIcon = ImageIO.read(getClass().getResourceAsStream("/button/button.png"));
            hoverIcon = ImageIO.read(getClass().getResourceAsStream("/button/button_highlighted.png"));
            currentIcon = normalIcon;
        } catch (IOException e) {
            log.error("Failed to load button icons.\n{}", e.getMessage());
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                currentIcon = hoverIcon;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                currentIcon = normalIcon;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (currentIcon != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            int width = getWidth();
            int height = getHeight();
            int imageWidth = currentIcon.getWidth();
            int imageHeight = currentIcon.getHeight();

            // Draw left border
            g2d.drawImage(
                    currentIcon,
                    0, 0, BORDER_SIZE, height,
                    0, 0, BORDER_SIZE, imageHeight,
                    this
            );

            // Draw right border
            g2d.drawImage(
                    currentIcon,
                    width - BORDER_SIZE, 0, width, height,
                    imageWidth - BORDER_SIZE, 0, imageWidth, imageHeight,
                    this
            );

            // Draw middle section
            g2d.drawImage(
                    currentIcon,
                    BORDER_SIZE, 0, width - BORDER_SIZE, height,
                    BORDER_SIZE, 0, imageWidth - BORDER_SIZE, imageHeight,
                    this
            );

            g2d.dispose();
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g2d.setColor(getForeground());
        g2d.drawString(getText(), textX, textY);
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        if (normalIcon != null) {
            int width = (int)(normalIcon.getWidth() * currentScale);
            int height = (int)(normalIcon.getHeight() * currentScale);
            return new Dimension(width, height);
        }
        return super.getPreferredSize();
    }

    @Override
    public double getDesignWidth() {
        return designSize.getWidth();
    }

    @Override
    public double getDesignHeight() {
        return designSize.getHeight();
    }

    @Override
    public void applyScale(double scaleFactor) {
        this.currentScale = scaleFactor;

        setFont(getFont().deriveFont((float)(getFont().getSize() * scaleFactor)));

        revalidate();
        repaint();
    }
}