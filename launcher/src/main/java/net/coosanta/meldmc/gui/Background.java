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
    private final StyleableIntegerProperty scaleFactor = new StyleableIntegerProperty(6) {
        @Override
        public Object getBean() {
            return Background.this;
        }

        @Override
        public String getName() {
            return "factor";
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.SCALE_FACTOR;
        }
    };

    public int getScaleFactor() {
        return scaleFactor.get();
    }

    public StyleableIntegerProperty scaleFactorProperty() {
        return scaleFactor;
    }

    public void setScaleFactor(int factor) {
        scaleFactor.set(factor);
    }

    private static class StyleableProperties {
        private static final CssMetaData<Background, Number> SCALE_FACTOR =
                new CssMetaData<>("-factor", SizeConverter.getInstance(), 6.0) {

                    @Override
                    public boolean isSettable(Background node) {
                        return !node.scaleFactor.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(Background node) {
                        return node.scaleFactor;
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Canvas.getClassCssMetaData());
            styleables.add(SCALE_FACTOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
    // Finish CSS handling.

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
        getStyleClass().add("texture-scale");
        redraw();
    }

    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
        redraw();
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
