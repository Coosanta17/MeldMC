package net.coosanta.totalityloader.gui.containers;

import net.coosanta.totalityloader.gui.GuiFrame;

public interface ScalablePanel {
    default double getDesignWidth() {
        return GuiFrame.DEFAULT_SIZE.width;
    };
    default double getDesignHeight() {
        return GuiFrame.DEFAULT_SIZE.height;
    };
    void applyScale(double scaleFactor);
}
