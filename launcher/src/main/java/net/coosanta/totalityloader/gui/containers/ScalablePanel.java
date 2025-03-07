package net.coosanta.totalityloader.gui.containers;

public interface ScalablePanel {
    double getDesignWidth();
    double getDesignHeight();
    void applyScale(double scaleFactor);
}
