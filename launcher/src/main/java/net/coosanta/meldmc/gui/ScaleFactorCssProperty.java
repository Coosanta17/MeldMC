package net.coosanta.meldmc.gui;

import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;

/**
 * ScaleFactorCssProperty provides a reusable mechanism for JavaFX components
 * to expose a CSS-styleable integer property for controlling scale factors.
 *
 * <p>This class helps create components that can have their scaling controlled via CSS
 * using the "-factor" property. For example, in CSS you could write:
 * <pre>
 * .texture-scale {
 *     -factor: 4;
 * }
 * </pre>
 *
 * <p>To use this class:
 * <ol>
 *   <li>Make your component implement the {@link ScaleFactorContainer} interface</li>
 *   <li>Create a ScaleFactorCssProperty instance in your component</li>
 *   <li>Implement getScaleFactorProperty() to return the property</li>
 *   <li>Add ScaleFactorCssProperty.getCssMetaData() to your component's CSS metadata</li>
 * </ol>
 *
 * <p>The default scale factor value is 6.
 *
 * @see ScaleFactorContainer
 * @see Background For an example implementation
 */
public class ScaleFactorCssProperty {
    private final Styleable owner;
    private final StyleableIntegerProperty property;
    private static final CssMetaData<Styleable, Number> CSS_META_DATA;

    static {
        CSS_META_DATA = new CssMetaData<>("-factor", SizeConverter.getInstance(), 6.0) {
            @Override
            public boolean isSettable(Styleable node) {
                @SuppressWarnings("unchecked")
                boolean result = node instanceof ScaleFactorCssProperty.ScaleFactorContainer &&
                        !((Property<Number>)((ScaleFactorCssProperty.ScaleFactorContainer)node).getScaleFactorProperty()).isBound();
                return result;
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Styleable node) {
                return ((ScaleFactorCssProperty.ScaleFactorContainer)node).getScaleFactorProperty();
            }
        };
    }

    /**
     * Interface to be implemented by JavaFX components that want to expose
     * a CSS-styleable scale factor property.
     *
     * <p>Classes implementing this interface should use a ScaleFactorCssProperty
     * instance to provide the actual property implementation.
     */
    public interface ScaleFactorContainer {
        /**
         * Returns the styleable property that represents the scale factor.
         *
         * @return The StyleableProperty controlling the scale factor
         */
        StyleableProperty<Number> getScaleFactorProperty();
    }

    /**
     * Creates a new ScaleFactorCssProperty.
     *
     * @param owner The Styleable component that owns this property
     * @param propertyName The name of the property (used for binding)
     */
    public ScaleFactorCssProperty(Styleable owner, String propertyName) {
        this.owner = owner;
        this.property = new StyleableIntegerProperty(6) {
            @Override
            public Object getBean() {
                return owner;
            }

            @Override
            public String getName() {
                return propertyName;
            }

            @Override
            public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                return CSS_META_DATA;
            }
        };
    }

    /**
     * Gets the current scale factor value.
     *
     * @return The current integer scale factor
     */
    public int get() {
        return property.get();
    }

    /**
     * Returns the underlying StyleableIntegerProperty.
     *
     * @return The StyleableIntegerProperty controlling the scale factor
     */
    public StyleableIntegerProperty property() {
        return property;
    }

    /**
     * Sets the scale factor value.
     *
     * @param value The new scale factor value
     */
    public void set(int value) {
        property.set(value);
    }

    /**
     * Returns the CSS metadata for the scale factor property.
     * Components using this class should include this metadata in their
     * getCssMetaData() implementations.
     *
     * @return The CssMetaData for the scale factor property
     */
    public static CssMetaData<? extends Styleable, Number> getCssMetaData() {
        return CSS_META_DATA;
    }
}
