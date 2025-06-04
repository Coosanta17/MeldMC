package net.coosanta.meldmc.gui.button;

import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;

public class MinecraftButtonSkin extends SkinBase<MinecraftButton> {

    public MinecraftButtonSkin(MinecraftButton control) {
        super(control);

        // The contentPane is already set up in the control, this adds it to the skin's children
        getChildren().add(control.getContentPane());
    }

    @Override
    protected void layoutChildren(final double x, final double y,
                                 final double w, final double h) {
        Region contentPane = (Region) getChildren().getFirst();
        contentPane.resizeRelocate(x, y, w, h);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computePrefWidth(height);
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computePrefHeight(width);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computePrefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computePrefHeight(width);
    }
}
