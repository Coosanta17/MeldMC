package net.coosanta.meldmc.gui;

import javafx.css.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import net.coosanta.meldmc.utility.ResourceUtil;
import net.coosanta.meldmc.utility.ScaleFactorCssProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Background extends Canvas implements ScaleFactorCssProperty.ScaleFactorContainer {
    private final ScaleFactorCssProperty scaleFactorProperty;
    private static final Image backgroundImage;
    private static final int BASE_TEXTURE_SIZE = 16;

    static {
        try {
            backgroundImage = new Image(ResourceUtil.loadResource("/icons/background.png").toExternalForm());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Background(double width, double height) {
        super(width, height);
        scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");
        ScaleFactorCssProperty.applyStandardTextureScale(this);
        redraw();
    }

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        redraw();
    }

    public int getScaleFactor() {
        return scaleFactorProperty.get();
    }

    public void setScaleFactor(int factor) {
        scaleFactorProperty.set(factor);
    }

    public StyleableIntegerProperty scaleFactorProperty() {
        return scaleFactorProperty.property();
    }

    @Override
    public StyleableProperty<Number> getScaleFactorProperty() {
        return scaleFactorProperty.property();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<>(Canvas.getClassCssMetaData());
        styleables.add(ScaleFactorCssProperty.getCssMetaData());
        return Collections.unmodifiableList(styleables);
    }

    public int calculateTileSize() {
        return BASE_TEXTURE_SIZE * getScaleFactor();
    }

    private void redraw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        int currentTileSize = calculateTileSize();

        for (int x = 0; x < getWidth(); x += currentTileSize) {
            for (int y = 0; y < getHeight(); y += currentTileSize) {
                gc.drawImage(backgroundImage, x, y, currentTileSize, currentTileSize);
            }
        }
        gc.setImageSmoothing(false);
    }
}
