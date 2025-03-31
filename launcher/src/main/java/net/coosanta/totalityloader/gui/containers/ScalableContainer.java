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
    protected Timer resizeWait;

    public ScalableContainer(JPanel innerPanel) {
        this.innerPanel = innerPanel;

        setLayout(new GridBagLayout());
        add(innerPanel);

        this.originalWidth = innerPanel instanceof ScalablePanel
                ? ((ScalablePanel) innerPanel).getDesignWidth()
                : Main.getInstance().getWindowSize().getWidth();
        this.originalHeight = innerPanel instanceof ScalablePanel
                ? ((ScalablePanel) innerPanel).getDesignHeight()
                : Main.getInstance().getWindowSize().getHeight();

        resizeWait = new Timer(150, e -> scale(innerPanel, this));
        resizeWait.setRepeats(false);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (resizeWait.isRunning()) {
                    resizeWait.restart();
                } else {
                    resizeWait.start();
                }
            }
        });
    }

    protected void scale(JPanel innerPanel, JPanel container) {
        int w = container.getWidth();
        int h = container.getHeight();

        double widthScaleFactor = (double) w / originalWidth;
        double heightScaleFactor = (double) h / originalHeight;

        scaleFactor = Math.min(widthScaleFactor, heightScaleFactor);

        innerPanel.setPreferredSize(new Dimension(w, h));

        applyScaleToInnerPanels(innerPanel);

        refreshGui(container);
    }

    protected void applyScaleToInnerPanels(JComponent innerComponent) {
        if (innerComponent instanceof ScalablePanel) {
            ((ScalablePanel) innerComponent).applyScale(getScaleFactor());
        }
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
