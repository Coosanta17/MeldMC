package net.coosanta.meldmc.gui;

import javafx.css.*;
import javafx.css.converter.SizeConverter;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import net.coosanta.meldmc.utility.ResourceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Background extends Canvas {
    // CSS handling
    private final StyleableIntegerProperty tileSize = new StyleableIntegerProperty(50) {
        @Override
        public Object getBean() {
            return Background.this;
        }

        @Override
        public String getName() {
            return "tileSize";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.TILE_SIZE;
        }
    };

    public int getTileSize() {
        return tileSize.get();
    }

    public StyleableIntegerProperty tileSizeProperty() {
        return tileSize;
    }

    public void setTileSize(int size) {
        tileSize.set(size);
    }

    private static class StyleableProperties {
        private static final CssMetaData<Background, Number> TILE_SIZE =
                new CssMetaData<>("-tile-size", SizeConverter.getInstance(), 50.0) {

                    @Override
                    public boolean isSettable(Background node) {
                        return !node.tileSize.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Background node) {
                        return node.tileSize;
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Canvas.getClassCssMetaData());
            styleables.add(TILE_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
    // Finish CSS handling.

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
        getStyleClass().add("background-tiles");
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

        int currentTileSize = tileSize.getValue();

        for (int x = 0; x < getWidth(); x += currentTileSize) {
            for (int y = 0; y < getHeight(); y += currentTileSize) {
                gc.drawImage(backgroundImage, x, y, currentTileSize, currentTileSize);
            }
        }
        gc.setImageSmoothing(false);
    }
}
