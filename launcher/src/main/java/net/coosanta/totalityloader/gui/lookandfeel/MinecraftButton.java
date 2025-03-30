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
    private static final Logger log = LoggerFactory.getLogger(MinecraftButton.class);

    private final BufferedImage originalDefaultIcon;
    private final BufferedImage originalDisabledIcon;
    private final BufferedImage originalHighlightedIcon;
    private final Font originalTextFont;

    private final double widthHeightRatio;
    private double currentScaleFactor = 1.0;

    public MinecraftButton(String text, boolean enabled) {
        this(text, enabled, new Dimension(200, 20));
    }

    public MinecraftButton(String text, boolean enabled, Dimension widthHeightFactor) {
        super(text);

        this.originalTextFont = getFont();

        this.widthHeightRatio = (double) (widthHeightFactor.width / widthHeightFactor.height);

        try {
            this.originalDefaultIcon = loadImage("/icons/button/button.png");
            this.originalDisabledIcon = loadImage("/icons/button/button_disabled.png");
            this.originalHighlightedIcon = loadImage("/icons/button/button_highlighted.png");
        } catch (IOException | NullPointerException e) {
            log.error("Failed to load button images", e);
            throw new RuntimeException("Failed to load button images", e);
        }

        setEnabled(enabled);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.CENTER);

        updateButtonAppearance();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    setIcon(new ImageIcon(createResizedImage(originalHighlightedIcon)));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateButtonAppearance();
            }
        });
    }

    private BufferedImage createResizedImage(BufferedImage original) {
        if (original == null) {
            log.error("Original image is null");
            throw new NullPointerException();
        }

        double ratio = Math.min(widthHeightRatio, 10.0);
        if (widthHeightRatio > 10.0) {
            log.error("Width-to-height ratio {} exceeds maximum 10:1, capping at 10:1", widthHeightRatio);
        }

        int originalHeight = original.getHeight();

        int targetHeight = (int) Math.round(originalHeight * currentScaleFactor);
        int targetWidth = (int) Math.round(targetHeight * ratio);

        targetHeight = Math.max(targetHeight, 1);
        targetWidth = Math.max(targetWidth, 4);

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int borderWidth = Math.max(1, Math.round((float)(2 * currentScaleFactor)));

        // Draw left border
        g.drawImage(
                original.getSubimage(0, 0, 2, original.getHeight()),
                0, 0, borderWidth, targetHeight,
                null
        );

        // Draw right border
        g.drawImage(
                original.getSubimage(original.getWidth() - 2, 0, 2, original.getHeight()),
                targetWidth - borderWidth, 0, borderWidth, targetHeight,
                null
        );

        // Draw middle section
        g.drawImage(
                original.getSubimage(2, 0, original.getWidth() - 4, original.getHeight()),
                borderWidth, 0, targetWidth - (2 * borderWidth), targetHeight,
                null
        );

        g.dispose();
        return resized;
    }

    private void updateButtonAppearance() {
        if (isEnabled()) {
            setIcon(new ImageIcon(createResizedImage(originalDefaultIcon)));
        } else {
            setIcon(new ImageIcon(createResizedImage(originalDisabledIcon)));
        }
        float newSize = (float) (originalTextFont.getSize() * currentScaleFactor);
        setFont(new Font(getFont().getName(), getFont().getStyle(), Math.round(newSize)));

        Icon icon = getIcon();
        if (icon != null) {
            setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        }
    }

    private BufferedImage loadImage(String path) throws IOException {
        return ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(path)));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateButtonAppearance();
    }

    @Override
    public void applyScale(double scaleFactor) {
        System.out.println("Button '" + getText() + "' scale changed: " +
                this.currentScaleFactor + " -> " + scaleFactor);
        this.currentScaleFactor = scaleFactor;
        updateButtonAppearance();
    }
}