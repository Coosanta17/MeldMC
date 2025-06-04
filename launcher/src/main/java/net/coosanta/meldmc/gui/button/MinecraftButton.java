package net.coosanta.meldmc.gui.button;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinecraftButton extends ButtonBase implements ScaleFactorCssProperty.ScaleFactorContainer {
    static final int ORIGINAL_WIDTH = 200;
    static final int ORIGINAL_HEIGHT = 20;

    static final int LEFT_BORDER = 2;
    static final int RIGHT_BORDER = 2;
    static final int TOP_BORDER = 2;
    static final int BOTTOM_BORDER = 3;

    private final ScaleFactorCssProperty scaleFactorProperty;

    private final ObjectProperty<Image> buttonImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> hoverButtonImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> disabledButtonImage = new SimpleObjectProperty<>();

    public MinecraftButton() {
        this("");
    }

    public MinecraftButton(String text) {
        this(text, false);
    }

    public MinecraftButton(String text, boolean disabled) {
        super();
        setText(text);
        setDisable(disabled);

        scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");
        getStyleClass().add("mc-button");

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        try {
            buttonImage.set(new Image("/icons/button/button.png"));
            hoverButtonImage.set(new Image("/icons/button/button_highlighted.png"));
            disabledButtonImage.set(new Image("/icons/button/button_disabled.png"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load button images", e);
        }

        scaleFactorProperty.property().addListener((obs, oldVal, newVal) -> requestLayout());
    }

    public void setFillWidth(boolean fill) {
        setMaxWidth(fill ? Double.MAX_VALUE : USE_PREF_SIZE);
    }

    public void setFillHeight(boolean fill) {
        setMaxHeight(fill ? Double.MAX_VALUE : USE_PREF_SIZE);
    }

    @Override
    public StyleableProperty<Number> getScaleFactorProperty() {
        return scaleFactorProperty.property();
    }

    public int getScaleFactor() {
        return scaleFactorProperty.get();
    }

    public Image getButtonImage() {
        return buttonImage.get();
    }

    public Image getHoverButtonImage() {
        return hoverButtonImage.get();
    }

    public Image getDisabledButtonImage() {
        return disabledButtonImage.get();
    }

    @Override
    protected double computePrefWidth(double height) {
        return Math.max(ORIGINAL_WIDTH * getScaleFactor(), computeMinWidth(height));
    }

    @Override
    protected double computePrefHeight(double width) {
        return Math.max(ORIGINAL_HEIGHT * getScaleFactor(), computeMinHeight(width));
    }

    @Override
    protected double computeMinWidth(double height) {
        int scaleFactor = getScaleFactor();
        return (LEFT_BORDER + RIGHT_BORDER) * scaleFactor + 20;
    }

    @Override
    protected double computeMinHeight(double width) {
        int scaleFactor = getScaleFactor();
        return (TOP_BORDER + BOTTOM_BORDER) * scaleFactor + 10;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MinecraftButtonSkin(this);
    }

    @Override
    public void fire() {
        if (!isDisabled()) {
            arm();
            fireEvent(new javafx.event.ActionEvent(this, null));
            javafx.application.Platform.runLater(this::disarm);
        }
    }

    // CSS integration
    private static final List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA;

    static {
        List<CssMetaData<? extends Styleable, ?>> cssMetaData = new ArrayList<>(ButtonBase.getClassCssMetaData());
        cssMetaData.add(ScaleFactorCssProperty.getCssMetaData());
        CSS_META_DATA = Collections.unmodifiableList(cssMetaData);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CSS_META_DATA;
    }
}
