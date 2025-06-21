package net.coosanta.meldmc.utility;

import javafx.beans.property.Property;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.Node;
import net.coosanta.meldmc.gui.views.Background;
import net.coosanta.meldmc.gui.nodes.button.MinecraftButton;

/**
 * ScaleFactorCssProperty provides a reusable mechanism for JavaFX components
 * to expose a CSS-styleable double property for controlling scale factors.
 *
 * <p>This class helps create components that can have their scaling controlled via CSS
 * using the "-scale-factor" property. For example, in CSS you could write:
 * <pre>
 * .texture-scale {
 *     -scale-factor: 4.5;
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
 * <p>The default scale factor value is 1.0.
 *
 * <p>Example implementation:
 * <pre>
 * public class ScalableComponent extends Region implements ScaleFactorContainer {
 *     private final ScaleFactorCssProperty scaleFactorProperty;
 *
 *     public ScalableComponent() {
 *         // Initialize the scale factor property
 *         scaleFactorProperty = new ScaleFactorCssProperty(this, "factor");
 *
 *         // Apply default texture scaling if desired
 *         ScaleFactorCssProperty.applyStandardTextureScale(this);
 *
 *         // Listen for scale factor changes and update layout
 *         scaleFactorProperty.property().addListener((obs, oldVal, newVal) -> requestLayout());
 *     }
 *
 *     // Implement the ScaleFactorContainer interface
 *     public StyleableProperty&lt;Number&gt; getScaleFactorProperty() {
 *         return scaleFactorProperty.property();
 *     }
 *
 *     // Get the current scale factor value
 *     public double getScaleFactor() {
 *         return scaleFactorProperty.get();
 *     }
 *
 *     // Add CSS metadata for styling
 *     public List&lt;CssMetaData&lt;? extends Styleable, ?&gt;&gt; getCssMetaData() {
 *         List&lt;CssMetaData&lt;? extends Styleable, ?&gt;&gt; cssMetaData =
 *                 new ArrayList&lt;&gt;(Region.getClassCssMetaData());
 *         cssMetaData.add(ScaleFactorCssProperty.getCssMetaData());
 *         return Collections.unmodifiableList(cssMetaData);
 *     }
 *
 *     protected double computePrefWidth(double height) {
 *         // Use scale factor in layout calculations
 *         return ORIGINAL_WIDTH * getScaleFactor();
 *     }
 * }
 * </pre>
 *
 * @see ScaleFactorContainer
 * @see Background For an example implementation
 * @see MinecraftButton For another example implementation
 */
public class ScaleFactorCssProperty {
    private final Styleable owner;
    private final StyleableDoubleProperty property;
    private static final CssMetaData<Styleable, Number> CSS_META_DATA;

    static {
        CSS_META_DATA = new CssMetaData<>("-scale-factor", SizeConverter.getInstance(), 1.0) {
            @Override
            public boolean isSettable(Styleable node) {
                @SuppressWarnings("unchecked")
                boolean result = node instanceof ScaleFactorCssProperty.ScaleFactorContainer &&
                        !((Property<Number>) ((ScaleFactorCssProperty.ScaleFactorContainer) node).getScaleFactorProperty()).isBound();
                return result;
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Styleable node) {
                return ((ScaleFactorCssProperty.ScaleFactorContainer) node).getScaleFactorProperty();
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
     * @param owner        The Styleable component that owns this property
     * @param propertyName The name of the property (used for binding)
     */
    public ScaleFactorCssProperty(Styleable owner, String propertyName) {
        this.owner = owner;
        this.property = new StyleableDoubleProperty(1) {
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
    public double get() {
        return property.get();
    }

    /**
     * Returns the underlying StyleableDoubleProperty.
     *
     * @return The StyleableDoubleProperty controlling the scale factor
     */
    public StyleableDoubleProperty property() {
        return property;
    }

    /**
     * Sets the scale factor value.
     *
     * @param value The new scale factor value
     */
    public void set(double value) {
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

    /**
     * Applies the standard texture scaling to the provided Styleable component.
     * This is a convenience method that adds the "texture-scale" CSS class to the component,
     * which is defined with the standard scaling factor of 1 in the base-style.css.
     *
     * <p>Use this method when you want to apply the standard texture scaling
     * without having to manually add the CSS class.
     *
     * @param styleable The component to apply the texture scaling to
     * @return The component with texture scaling applied (for method chaining)
     */
    public static Styleable applyStandardTextureScale(Styleable styleable) {
        if (styleable instanceof Node node) {
            node.getStyleClass().add("texture-scale");
        }
        return styleable;
    }
}
