package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class Container extends JPanel {
    protected final double originalWidth;
    protected final double originalHeight;
    protected double scaleFactor = 1.0;

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
                scale(innerPanel, container);
                refreshGui(container);
            }
        });
    }

    protected void scale(JPanel innerPanel, JPanel container) {
        int w = container.getWidth();
        int h = container.getHeight();
        int size = Math.min(w, h);

        scaleFactor = (double) size / Math.min(originalWidth, originalHeight);

        innerPanel.setPreferredSize(new Dimension(w, h));

        if (innerPanel instanceof ScalablePanel) {
            ((ScalablePanel) innerPanel).applyScale(scaleFactor);
        }

        refreshGui(container);
    }
}
