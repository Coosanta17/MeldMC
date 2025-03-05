package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class FixedRatioContainer extends Container {
    public FixedRatioContainer(JPanel innerPanel) {
        super(innerPanel);
    }

    @Override
    protected void scale(JPanel innerPanel, JPanel container) {
        int w = container.getWidth();
        int h = container.getHeight();
        int size = Math.min(w, h);

        scaleFactor = (double) size / Math.min(originalWidth, originalHeight);

        innerPanel.setPreferredSize(new Dimension(size, size));

        if (innerPanel instanceof ScalablePanel) {
            ((ScalablePanel) innerPanel).applyScale(scaleFactor);
        }

        refreshGui(container);
    }
}
