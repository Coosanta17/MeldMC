package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static net.coosanta.totalityloader.gui.Gui.refreshGui;

public class Container extends JPanel {
    private final double originalWidth;
    private final double originalHeight;
    private double scaleFactor = 1.0;

    public Container(JPanel innerPanel) {
        JPanel container = this;
        setLayout(new GridBagLayout());
        add(innerPanel);

        this.originalWidth = innerPanel instanceof ScalablePanel
                ? ((ScalablePanel) innerPanel).getDesignWidth()
                : Main.getInstance().getWindowSize().getWidth();
        this.originalHeight = innerPanel instanceof ScalablePanel
                ? ((ScalablePanel) innerPanel).getDesignHeight()
                : Main.getInstance().getWindowSize().getHeight();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeAndScale(innerPanel, container);
                refreshGui(container);
            }
        });
    }

    private void resizeAndScale(JPanel innerPanel, JPanel container) {
        int w = container.getWidth();
        int h = container.getHeight();
        int size = Math.min(w, h);

        // Calculate scaling factor based on original size
        scaleFactor = (double) size / Math.min(originalWidth, originalHeight);

        innerPanel.setPreferredSize(new Dimension(size, size));

        // Apply scaling to the inner panel if it supports scaling
        if (innerPanel instanceof ScalablePanel) {
            ((ScalablePanel) innerPanel).applyScale(scaleFactor);
        }

        refreshGui(container);
    }
}
