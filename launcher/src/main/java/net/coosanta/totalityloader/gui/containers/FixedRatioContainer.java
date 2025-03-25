package net.coosanta.totalityloader.gui.containers;

import javax.swing.*;
import java.awt.*;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class FixedRatioContainer extends ScalableContainer {
    private final double widthFactor;
    private final double heightFactor;

    // default 1:1
    public FixedRatioContainer(JPanel innerPanel) {
        this(innerPanel, 1, 1);
    }

    public FixedRatioContainer(JPanel innerPanel, double widthFactor, double heightFactor) {
        super(innerPanel);
        this.widthFactor = widthFactor;
        this.heightFactor = heightFactor;
    }

    @Override
    protected void scale(JPanel innerPanel, JPanel container) {
        int w = container.getWidth();
        int h = container.getHeight();

        // Calculate dimensions based on the specified ratio
        int newWidth, newHeight;
        double containerRatio = (double) w / h;
        double targetRatio = widthFactor / heightFactor;

        if (containerRatio > targetRatio) {
            // Container is wider than needed, height is the limiting factor
            newHeight = h;
            newWidth = (int) (h * targetRatio);
        } else {
            // Container is taller than needed, width is the limiting factor
            newWidth = w;
            newHeight = (int) (w / targetRatio);
        }

        scaleFactor = Math.min(
                (double) newWidth / originalWidth,
                (double) newHeight / originalHeight
        );

        innerPanel.setPreferredSize(new Dimension(newWidth, newHeight));

        if (innerPanel instanceof ScalablePanel) {
            ((ScalablePanel) innerPanel).applyScale(scaleFactor);
        }

        refreshGui(container);
    }
}