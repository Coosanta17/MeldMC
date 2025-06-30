package net.coosanta.meldmc.gui.nodes.button;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import net.coosanta.meldmc.utility.TextureScalingUtil;

public class MinecraftButtonSkin extends SkinBase<MinecraftButton> {
    private final Canvas canvas;
    private final Label textLabel;
    private final StackPane contentPane;
    private boolean hovered = false;

    public MinecraftButtonSkin(MinecraftButton control) {
        super(control);

        canvas = new Canvas();
        textLabel = new Label();
        textLabel.getStyleClass().add("mc-button-text");
        textLabel.textProperty().bind(control.textProperty());

        contentPane = new StackPane(canvas, textLabel);
        getChildren().add(contentPane);

        setupEventHandling(control);

        control.widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        control.heightProperty().addListener((obs, oldVal, newVal) -> redraw());
    }

    private void setupEventHandling(MinecraftButton button) {
        button.setOnMouseEntered(e -> {
            hovered = true;
            redraw();
        });

        button.setOnMouseExited(e -> {
            hovered = false;
            redraw();
        });

        button.setOnMousePressed(e -> {
            if (!button.isDisabled()) {
                button.fire();
                redraw();
            }
        });

        button.armedProperty().addListener((obs, wasArmed, isArmed) -> redraw());

        button.disabledProperty().addListener((obs, wasDisabled, isDisabled) -> redraw());
    }

    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h) {
        contentPane.resizeRelocate(x, y, w, h);

        canvas.setWidth(w);
        canvas.setHeight(h);

        redraw();
    }

    private void redraw() {
        MinecraftButton button = getSkinnable();

        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }

        Image image;
        if (button.isDisabled()) {
            image = button.getDisabledButtonImage();
        } else if (button.isArmed() || hovered) {
            image = button.getHoverButtonImage();
        } else {
            image = button.getButtonImage();
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setImageSmoothing(false);

        drawButton(gc, image);
    }

    private void drawButton(GraphicsContext gc, Image image) {
        double scaleFactor = getSkinnable().getScaleFactor();

        TextureScalingUtil.drawNineSliceTexture(
            gc, image,
            MinecraftButton.LEFT_BORDER, MinecraftButton.RIGHT_BORDER,
            MinecraftButton.TOP_BORDER, MinecraftButton.BOTTOM_BORDER,
            0, 0, canvas.getWidth(), canvas.getHeight(),
            scaleFactor
        );
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computeMinWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computeMinHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computePrefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().computePrefHeight(width) + topInset + bottomInset;
    }
}
