package net.coosanta.meldmc.gui.button;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinecraftButton extends Control implements ScaleFactorCssProperty.ScaleFactorContainer {
    private final Canvas canvas;
    private final Label textLabel;
    private final StackPane contentPane;

    private final ScaleFactorCssProperty scaleFactorProperty;

    private final ObjectProperty<Image> buttonImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> hoverButtonImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> disabledButtonImage = new SimpleObjectProperty<>();

    private static final int ORIGINAL_WIDTH = 200;
    private static final int ORIGINAL_HEIGHT = 20;

    private boolean hovered = false;

    private static final int LEFT_BORDER = 2;
    private static final int RIGHT_BORDER = 2;
    private static final int TOP_BORDER = 2;
    private static final int BOTTOM_BORDER = 3;

    public MinecraftButton() {
        this("");
    }

    public MinecraftButton(String text) {
        this(text, false);
    }

    public MinecraftButton(String text, boolean disabled) {
        setDisable(disabled);
        canvas = new Canvas();
        textLabel = new Label(text);
        textLabel.getStyleClass().add("minecraft-button-text");

        try {
            buttonImage.set(new Image("/icons/button/button.png"));
            hoverButtonImage.set(new Image("/icons/button/button_highlighted.png"));
            disabledButtonImage.set(new Image("/icons/button/button_disabled.png"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load button images", e);
        }

        contentPane = new StackPane();
        contentPane.getChildren().addAll(canvas, textLabel);
        contentPane.setAlignment(Pos.CENTER);

        scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");
        ScaleFactorCssProperty.applyStandardTextureScale(this);

        setupEventHandling();

        widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        heightProperty().addListener((obs, oldVal, newVal) -> redraw());
        scaleFactorProperty.property().addListener((obs, oldVal, newVal) -> redraw());

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        setFillWidth(true);
        setFillHeight(true);
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

    private void setupEventHandling() {
        setOnMouseEntered(e -> {
            hovered = true;
            redraw();
        });

        setOnMouseExited(e -> {
            hovered = false;
            redraw();
        });
    }

    public String getText() {
        return textLabel.getText();
    }

    public void setText(String value) {
        textLabel.setText(value);
    }

    public StringProperty textProperty() {
        return textLabel.textProperty();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        contentPane.resize(getWidth(), getHeight());
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    @Override
    protected double computePrefWidth(double height) {
        return Math.max(ORIGINAL_WIDTH * scaleFactorProperty.get(), getMinWidth());
    }

    @Override
    protected double computePrefHeight(double width) {
        return Math.max(ORIGINAL_HEIGHT * scaleFactorProperty.get(), getMinHeight());
    }

    @Override
    public double computeMinWidth(double height) {
        return LEFT_BORDER * scaleFactorProperty.get() + RIGHT_BORDER * scaleFactorProperty.get() + 20;
    }

    @Override
    public double computeMinHeight(double width) {
        return TOP_BORDER * scaleFactorProperty.get() + BOTTOM_BORDER * scaleFactorProperty.get() + 10;
    }

    private void redraw() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }

        Image image;
        if (isDisable()) {
            image = disabledButtonImage.get();
        } else if (hovered) {
            image = hoverButtonImage.get();
        } else {
            image = buttonImage.get();
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);

        int scaleFactor = scaleFactorProperty.get();

        int scaledLeft = LEFT_BORDER * scaleFactor;
        int scaledRight = RIGHT_BORDER * scaleFactor;
        int scaledTop = TOP_BORDER * scaleFactor;
        int scaledBottom = BOTTOM_BORDER * scaleFactor;

        // Ensure borders snap to pixels perfectly
        scaledLeft = snapToScale(scaledLeft, scaleFactor);
        scaledRight = snapToScale(scaledRight, scaleFactor);
        scaledTop = snapToScale(scaledTop, scaleFactor);
        scaledBottom = snapToScale(scaledBottom, scaleFactor);

        // Calculate center regions dimensions
        double centerWidth = getWidth() - scaledLeft - scaledRight;
        double centerHeight = getHeight() - scaledTop - scaledBottom;

        // Top-left corner
        gc.drawImage(
                image,
                0, 0, LEFT_BORDER, TOP_BORDER,
                0, 0, scaledLeft, scaledTop
        );

        // Top edge
        gc.drawImage(
                image,
                LEFT_BORDER, 0, ORIGINAL_WIDTH - LEFT_BORDER - RIGHT_BORDER, TOP_BORDER,
                scaledLeft, 0, centerWidth, scaledTop
        );

        // Top-right corner
        gc.drawImage(
                image,
                ORIGINAL_WIDTH - RIGHT_BORDER, 0, RIGHT_BORDER, TOP_BORDER,
                scaledLeft + centerWidth, 0, scaledRight, scaledTop
        );

        // Left edge
        gc.drawImage(
                image,
                0, TOP_BORDER, LEFT_BORDER, ORIGINAL_HEIGHT - TOP_BORDER - BOTTOM_BORDER,
                0, scaledTop, scaledLeft, centerHeight
        );

        // Center
        gc.drawImage(
                image,
                LEFT_BORDER, TOP_BORDER, ORIGINAL_WIDTH - LEFT_BORDER - RIGHT_BORDER, ORIGINAL_HEIGHT - TOP_BORDER - BOTTOM_BORDER,
                scaledLeft, scaledTop, centerWidth, centerHeight
        );

        // Right edge
        gc.drawImage(
                image,
                ORIGINAL_WIDTH - RIGHT_BORDER, TOP_BORDER, RIGHT_BORDER, ORIGINAL_HEIGHT - TOP_BORDER - BOTTOM_BORDER,
                scaledLeft + centerWidth, scaledTop, scaledRight, centerHeight
        );

        // Bottom-left corner
        gc.drawImage(
                image,
                0, ORIGINAL_HEIGHT - BOTTOM_BORDER, LEFT_BORDER, BOTTOM_BORDER,
                0, scaledTop + centerHeight, scaledLeft, scaledBottom
        );

        // Bottom edge
        gc.drawImage(
                image,
                LEFT_BORDER, ORIGINAL_HEIGHT - BOTTOM_BORDER, ORIGINAL_WIDTH - LEFT_BORDER - RIGHT_BORDER, BOTTOM_BORDER,
                scaledLeft, scaledTop + centerHeight, centerWidth, scaledBottom
        );

        // Bottom-right corner
        gc.drawImage(
                image,
                ORIGINAL_WIDTH - RIGHT_BORDER, ORIGINAL_HEIGHT - BOTTOM_BORDER, RIGHT_BORDER, BOTTOM_BORDER,
                scaledLeft + centerWidth, scaledTop + centerHeight, scaledRight, scaledBottom
        );
    }

    private int snapToScale(int value, int scaleFactor) {
        return (int) (Math.round((double) value / scaleFactor) * scaleFactor);
    }

    // CSS integration

    private static final List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA;

    static {
        List<CssMetaData<? extends Styleable, ?>> cssMetaData = new ArrayList<>(Control.getClassCssMetaData());
        cssMetaData.add(ScaleFactorCssProperty.getCssMetaData());
        CSS_META_DATA = Collections.unmodifiableList(cssMetaData);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CSS_META_DATA;
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    StackPane getContentPane() {
        return contentPane;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MinecraftButtonSkin(this);
    }
}
