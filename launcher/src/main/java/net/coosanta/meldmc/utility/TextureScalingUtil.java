package net.coosanta.meldmc.utility;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Utility class for texture scaling operations, currently only 9-slice scaling.
 */
public class TextureScalingUtil {

    /**
     * Draws a texture using the tiling approach.
     * This method repeats the source region of the texture to fill the destination area.
     *
     * @param gc          The graphics context to draw on
     * @param image       The source image
     * @param sx          The source x coordinate
     * @param sy          The source y coordinate
     * @param sw          The source width
     * @param sh          The source height
     * @param dx          The destination x coordinate
     * @param dy          The destination y coordinate
     * @param dw          The destination width
     * @param dh          The destination height
     * @param scaleFactor The scale factor to apply
     */
    public static void drawTextureTiles(GraphicsContext gc, Image image,
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

    /**
     * Draws a texture using 9-slice scaling.
     * This method splits the source image into 9 regions and scales them appropriately to fill the destination.
     *
     * @param gc           The graphics context to draw on
     * @param image        The source image
     * @param leftBorder   Width of left border
     * @param rightBorder  Width of right border
     * @param topBorder    Height of top border
     * @param bottomBorder Height of bottom border
     * @param dx           The destination x coordinate
     * @param dy           The destination y coordinate
     * @param dw           The destination width
     * @param dh           The destination height
     * @param scaleFactor  The scale factor to apply
     */
    public static void drawNineSliceTexture(GraphicsContext gc, Image image,
                                            int leftBorder, int rightBorder, int topBorder, int bottomBorder,
                                            double dx, double dy, double dw, double dh,
                                            double scaleFactor) {
        if (image == null) return;

        int srcWidth = (int) image.getWidth();
        int srcHeight = (int) image.getHeight();

        int middleWidth = srcWidth - leftBorder - rightBorder;
        int middleHeight = srcHeight - topBorder - bottomBorder;

        if (middleWidth <= 0 || middleHeight <= 0) return;

        double scaledLeftBorder = leftBorder * scaleFactor;
        double scaledRightBorder = rightBorder * scaleFactor;
        double scaledTopBorder = topBorder * scaleFactor;
        double scaledBottomBorder = bottomBorder * scaleFactor;

        double destMiddleWidth = dw - scaledLeftBorder - scaledRightBorder;
        double destMiddleHeight = dh - scaledTopBorder - scaledBottomBorder;

        if (destMiddleWidth <= 0 || destMiddleHeight <= 0) return;

        // Top-left corner
        gc.drawImage(image, 0, 0, leftBorder, topBorder,
                dx, dy, scaledLeftBorder, scaledTopBorder);

        // Top edge
        drawTextureTiles(gc, image,
                leftBorder, 0, middleWidth, topBorder,
                dx + scaledLeftBorder, dy, destMiddleWidth, scaledTopBorder,
                scaleFactor);

        // Top-right corner
        gc.drawImage(image, srcWidth - rightBorder, 0, rightBorder, topBorder,
                dx + dw - scaledRightBorder, dy, scaledRightBorder, scaledTopBorder);

        // Left edge
        drawTextureTiles(gc, image,
                0, topBorder, leftBorder, middleHeight,
                dx, dy + scaledTopBorder, scaledLeftBorder, destMiddleHeight,
                scaleFactor);

        // Middle
        drawTextureTiles(gc, image,
                leftBorder, topBorder, middleWidth, middleHeight,
                dx + scaledLeftBorder, dy + scaledTopBorder, destMiddleWidth, destMiddleHeight,
                scaleFactor);

        // Right edge
        drawTextureTiles(gc, image,
                srcWidth - rightBorder, topBorder, rightBorder, middleHeight,
                dx + dw - scaledRightBorder, dy + scaledTopBorder, scaledRightBorder, destMiddleHeight,
                scaleFactor);

        // Bottom-left corner
        gc.drawImage(image, 0, srcHeight - bottomBorder, leftBorder, bottomBorder,
                dx, dy + dh - scaledBottomBorder, scaledLeftBorder, scaledBottomBorder);

        // Bottom edge
        drawTextureTiles(gc, image,
                leftBorder, srcHeight - bottomBorder, middleWidth, bottomBorder,
                dx + scaledLeftBorder, dy + dh - scaledBottomBorder, destMiddleWidth, scaledBottomBorder,
                scaleFactor);

        // Bottom-right corner
        gc.drawImage(image, srcWidth - rightBorder, srcHeight - bottomBorder, rightBorder, bottomBorder,
                dx + dw - scaledRightBorder, dy + dh - scaledBottomBorder, scaledRightBorder, scaledBottomBorder);
    }
}
