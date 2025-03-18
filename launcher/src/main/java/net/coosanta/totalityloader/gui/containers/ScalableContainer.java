package net.coosanta.totalityloader.gui.containers;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.gui.lookandfeel.TransparentPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class ScalableContainer extends TransparentPanel implements ScalablePanel {
    protected final double originalWidth;
    protected final double originalHeight;
    protected double scaleFactor = 1.0;
    protected final JPanel innerPanel;

    public ScalableContainer(JPanel innerPanel) {
        this.innerPanel = innerPanel;

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

    public double getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public double getDesignWidth() {
        return originalWidth;
    }

    @Override
    public double getDesignHeight() {
        return originalHeight;
    }

    @Override
    public void applyScale(double scaleFactor) {
        scale(innerPanel, this);
    }
}
