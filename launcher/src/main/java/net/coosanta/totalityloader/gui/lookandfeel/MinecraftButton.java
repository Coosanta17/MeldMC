package net.coosanta.totalityloader.gui.lookandfeel;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class MinecraftButton extends JButton implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(MinecraftButton.class);
    private final Dimension designSize;

    private Image normalIcon;
    private Image hoverIcon;
    private Image currentIcon;
    private boolean isHovered = false;

    public MinecraftButton(String text, Dimension designSize) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        this.designSize = designSize;

        try {
            // Load your button images - adjust paths as needed
            normalIcon = ImageIO.read(getClass().getResourceAsStream("/button/button.png"));
            hoverIcon = ImageIO.read(getClass().getResourceAsStream("/button/button_highlighted"));
            currentIcon = normalIcon;
        } catch (IOException e) {
            log.error("Failed to load button icons.\n{}", String.valueOf(e));
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                currentIcon = hoverIcon;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                currentIcon = normalIcon;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (currentIcon != null) {
            g.drawImage(currentIcon, 0, 0, getWidth(), getHeight(), this);
        }

        // Draw text centered on the button
        FontMetrics fm = g.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g.setColor(getForeground());
        g.drawString(getText(), textX, textY);
    }

    @Override
    public Dimension getPreferredSize() {
        if (normalIcon != null) {
            int width = normalIcon.getWidth(this);
            int height = normalIcon.getHeight(this);
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

    }
}
