package net.coosanta.totalityloader.gui;

public interface ScalablePanel {
    double getDesignWidth();
    double getDesignHeight();
    void applyScale(double scaleFactor);
}
