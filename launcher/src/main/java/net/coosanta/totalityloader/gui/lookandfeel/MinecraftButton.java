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
import java.util.Objects;

public class MinecraftButton extends JButton implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(MinecraftButton.class);
    private final Dimension designSize;
    private double currentScale = 1.0;

    private BufferedImage normalIcon;
    private BufferedImage hoverIcon;
    private BufferedImage disabledIcon;

    private BufferedImage originalNormalIcon;
    private BufferedImage originalHoverIcon;
    private BufferedImage originalDisabledIcon;

    private BufferedImage currentIcon;

    private boolean active;

    // Size of the left and right borders of the button in pixels.
    private static final int BORDER_SIZE = 2;

    public MinecraftButton(String text, boolean active) {
        this(text, null, active);
    }

    public MinecraftButton(String text, Dimension designSize, boolean active) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        this.designSize = designSize != null ? designSize : new Dimension(200, 20);
        this.active = active;

        try {
            originalNormalIcon = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/button/button.png")));
            originalHoverIcon = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/button/button_highlighted.png")));
            originalDisabledIcon = ImageIO.read(Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/button/button_disabled.png")));

            currentIcon = (active) ? normalIcon : disabledIcon;
        } catch (IOException e) {
            log.error("Failed to load button icons.\n{}", e.getMessage());
        } catch (NullPointerException e) {
            throw new RuntimeException("Cannot find button icons.", e);
        }

        normalIcon = originalNormalIcon;
        hoverIcon = originalHoverIcon;
        disabledIcon = originalDisabledIcon;
        currentIcon = (active) ? normalIcon : disabledIcon;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (active) {
                    currentIcon = hoverIcon;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (active) {
                    currentIcon = normalIcon;
                    repaint();
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            currentIcon = disabledIcon;
        } else {
            currentIcon = normalIcon;
        }
        repaint();
    }

    public boolean isActive() {
        return active;
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
        } else {
            log.error("currentIcon is null!");
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
        if (designSize != null) {
            return new Dimension(
                    (int) (designSize.width * currentScale),
                    (int) (designSize.height * currentScale)
            );
        }

        if (normalIcon != null) {
            int width = (int) (normalIcon.getWidth() * currentScale);
            int height = (int) (normalIcon.getHeight() * currentScale);
            return new Dimension(width, height);
        }

        return super.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize() {
        // Allow the button to be resized to any size
        return new Dimension(10, 10);
    }

    @Override
    public Dimension getMaximumSize() {
        // Return parent's size to fill available space
        return getParent() != null ? getParent().getSize() : super.getMaximumSize();
    }

    @Override
    public double getDesignWidth() {
        return designSize.getWidth();
    }

    @Override
    public double getDesignHeight() {
        return designSize.getHeight();
    }

    private static BufferedImage scaleImage(BufferedImage original, double scale) {
        if (original == null) return null;

        int newWidth = (int) (original.getWidth() * scale);
        int newHeight = (int) (original.getHeight() * scale);

        // Prevents scaling to zero dimensions
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return scaled;
    }

    @Override
    public void applyScale(double scaleFactor) {
        this.currentScale = scaleFactor;

        setFont(getFont().deriveFont((float) (getFont().getSize() * scaleFactor)));

        normalIcon = scaleImage(originalNormalIcon, scaleFactor);
        hoverIcon = scaleImage(originalHoverIcon, scaleFactor);
        disabledIcon = scaleImage(originalDisabledIcon, scaleFactor);

        currentIcon = active ? normalIcon : disabledIcon;

        revalidate();
        repaint();
    }
}