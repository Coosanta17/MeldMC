package net.coosanta.meldmc.gui.button;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

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

        double scaleFactor = button.getScaleFactor();

        double scaledLeft = MinecraftButton.LEFT_BORDER * scaleFactor;
        double scaledRight = MinecraftButton.RIGHT_BORDER * scaleFactor;
        double scaledTop = MinecraftButton.TOP_BORDER * scaleFactor;
        double scaledBottom = MinecraftButton.BOTTOM_BORDER * scaleFactor;

        double centerWidth = canvas.getWidth() - scaledLeft - scaledRight;
        double centerHeight = canvas.getHeight() - scaledTop - scaledBottom;

        drawButton(gc, image, scaledLeft, scaledTop, centerWidth, centerHeight);
    }

    private void drawButton(GraphicsContext gc, Image image,
                            double scaledLeft, double scaledTop,
                            double centerWidth, double centerHeight) {
        double scaleFactor = getSkinnable().getScaleFactor();

        // Top-left corner
        drawTextureTiles(gc, image,
                0, 0,
                MinecraftButton.LEFT_BORDER, MinecraftButton.TOP_BORDER,
                0, 0,
                MinecraftButton.LEFT_BORDER * scaleFactor, MinecraftButton.TOP_BORDER * scaleFactor,
                scaleFactor);

        // Top edge
        drawTextureTiles(gc, image,
                MinecraftButton.LEFT_BORDER, 0,
                MinecraftButton.ORIGINAL_WIDTH - MinecraftButton.LEFT_BORDER - MinecraftButton.RIGHT_BORDER,
                MinecraftButton.TOP_BORDER,
                scaledLeft, 0,
                centerWidth,
                MinecraftButton.TOP_BORDER * scaleFactor,
                scaleFactor);

        // Top-right corner
        drawTextureTiles(gc, image,
                MinecraftButton.ORIGINAL_WIDTH - MinecraftButton.RIGHT_BORDER, 0,
                MinecraftButton.RIGHT_BORDER, MinecraftButton.TOP_BORDER,
                scaledLeft + centerWidth, 0,
                MinecraftButton.RIGHT_BORDER * scaleFactor,
                MinecraftButton.TOP_BORDER * scaleFactor,
                scaleFactor);

        // Left edge
        drawTextureTiles(gc, image,
                0, MinecraftButton.TOP_BORDER,
                MinecraftButton.LEFT_BORDER,
                MinecraftButton.ORIGINAL_HEIGHT - MinecraftButton.TOP_BORDER - MinecraftButton.BOTTOM_BORDER,
                0, scaledTop,
                MinecraftButton.LEFT_BORDER * scaleFactor,
                centerHeight,
                scaleFactor);

        // Centre
        drawTextureTiles(gc, image,
                MinecraftButton.LEFT_BORDER, MinecraftButton.TOP_BORDER,
                MinecraftButton.ORIGINAL_WIDTH - MinecraftButton.LEFT_BORDER - MinecraftButton.RIGHT_BORDER,
                MinecraftButton.ORIGINAL_HEIGHT - MinecraftButton.TOP_BORDER - MinecraftButton.BOTTOM_BORDER,
                scaledLeft, scaledTop, centerWidth, centerHeight, scaleFactor);

        // Right edge
        drawTextureTiles(gc, image,
                MinecraftButton.ORIGINAL_WIDTH - MinecraftButton.RIGHT_BORDER, MinecraftButton.TOP_BORDER,
                MinecraftButton.RIGHT_BORDER,
                MinecraftButton.ORIGINAL_HEIGHT - MinecraftButton.TOP_BORDER - MinecraftButton.BOTTOM_BORDER,
                scaledLeft + centerWidth, scaledTop,
                MinecraftButton.RIGHT_BORDER * scaleFactor,
                centerHeight,
                scaleFactor);

        // Bottom-left corner
        drawTextureTiles(gc, image,
                0, MinecraftButton.ORIGINAL_HEIGHT - MinecraftButton.BOTTOM_BORDER,
                MinecraftButton.LEFT_BORDER, MinecraftButton.BOTTOM_BORDER,
                0, scaledTop + centerHeight,
                MinecraftButton.LEFT_BORDER * scaleFactor,
                MinecraftButton.BOTTOM_BORDER * scaleFactor,
                scaleFactor);

        // Bottom edge
        drawTextureTiles(gc, image,
                MinecraftButton.LEFT_BORDER, MinecraftButton.ORIGINAL_HEIGHT - MinecraftButton.BOTTOM_BORDER,
                MinecraftButton.ORIGINAL_WIDTH - MinecraftButton.LEFT_BORDER - MinecraftButton.RIGHT_BORDER,
                MinecraftButton.BOTTOM_BORDER,
                scaledLeft, scaledTop + centerHeight,
                centerWidth,
                MinecraftButton.BOTTOM_BORDER * scaleFactor,
                scaleFactor);

        // Bottom-right corner
        drawTextureTiles(gc, image,
                MinecraftButton.ORIGINAL_WIDTH - MinecraftButton.RIGHT_BORDER,
                MinecraftButton.ORIGINAL_HEIGHT - MinecraftButton.BOTTOM_BORDER,
                MinecraftButton.RIGHT_BORDER, MinecraftButton.BOTTOM_BORDER,
                scaledLeft + centerWidth, scaledTop + centerHeight,
                MinecraftButton.RIGHT_BORDER * scaleFactor,
                MinecraftButton.BOTTOM_BORDER * scaleFactor,
                scaleFactor);
    }

    private void drawTextureTiles(GraphicsContext gc, Image image,
                                  double sx, double sy, double sw, double sh,
                                  double dx, double dy, double dw, double dh,
                                  double scaleFactor) {
        int tilesX = (int) Math.ceil(dw / (sw * scaleFactor));
        int tilesY = (int) Math.ceil(dh / (sh * scaleFactor));

        double tileWidth = sw * scaleFactor;
        double tileHeight = sh * scaleFactor;

        for (int y = 0; y < tilesY; y++) {
            for (int x = 0; x < tilesX; x++) {
                double currentTileWidth = Math.min(tileWidth, dw - x * tileWidth);
                double currentTileHeight = Math.min(tileHeight, dh - y * tileHeight);

                if (currentTileWidth <= 0 || currentTileHeight <= 0) continue;

                double currentSrcWidth = currentTileWidth / scaleFactor;
                double currentSrcHeight = currentTileHeight / scaleFactor;

                gc.drawImage(
                        image,
                        sx, sy, currentSrcWidth, currentSrcHeight,
                        dx + x * tileWidth, dy + y * tileHeight,
                        currentTileWidth, currentTileHeight
                );
            }
        }
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
