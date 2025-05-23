package net.coosanta.meldmc.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import net.coosanta.meldmc.utility.ResourceUtil;

import java.io.IOException;

public class Background extends Canvas {
    private static final int TILE_SIZE = 100;
    private static final Image backgroundImage;

    static {
        try {
            backgroundImage = new Image(ResourceUtil.loadResource("/icons/background.png").toExternalForm());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Background(double width, double height) {
        super(width, height);
        redraw();
    }

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        redraw();
    }

    private void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (int x = 0; x < getWidth(); x += TILE_SIZE) {
            for (int y = 0; y < getHeight(); y += TILE_SIZE) {
                gc.drawImage(backgroundImage, x, y, TILE_SIZE, TILE_SIZE);
            }
        }
        gc.setImageSmoothing(false);
    }
}
