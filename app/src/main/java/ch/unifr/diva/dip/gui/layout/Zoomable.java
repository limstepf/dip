package ch.unifr.diva.dip.gui.layout;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Interface of a zoomable component.
 */
public interface Zoomable {

	/**
	 * Interpolation method used for zooming/resampling.
	 */
	public enum Interpolation {

		/**
		 * Nearest-neighbor interpolation.
		 */
		NEAREST_NEIGHBOR,
		/**
		 * Bilinear interpolation.
		 */
		BILINEAR;

		/**
		 * Savely returns the interpolation method by its name.
		 *
		 * @param name the name of the interpolation method.
		 * @return the requested interpolation method, or the default one if
		 * invalid.
		 */
		public static Interpolation get(String name) {
			try {
				return Interpolation.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return getDefault();
			}
		}

		/**
		 * Returns the default interpolation method.
		 *
		 * @return the default interpolation method.
		 */
		public static Interpolation getDefault() {
			return BILINEAR;
		}

	}

	/**
	 * Returns the root node of the zoomable component.
	 *
	 * @return the root node (a region even) of the zoomable component.
	 */
	public Region getNode();

	/**
	 * The height property of the root node/region of the zoomable component.
	 *
	 * @return the height property of the root node/region.
	 */
	default ReadOnlyDoubleProperty heightProperty() {
		return getNode().heightProperty();
	}

	/**
	 * The width property of the root node/region of the zoomable component.
	 *
	 * @return the width property of the root node/region.
	 */
	default ReadOnlyDoubleProperty widthProperty() {
		return getNode().widthProperty();
	}

	/**
	 * The zoom property.
	 *
	 * @return the zoom property ({@code 1.0} = 100%}).
	 */
	public DoubleProperty zoomProperty();

	/**
	 * Sets the zoom factor.
	 *
	 * @param value the zoom factor ({@code 1.0} = 100%}).
	 */
	default void setZoom(double value) {
		zoomProperty().set(value);
	}

	/**
	 * Returns the zoom factor.
	 *
	 * @return the zoom factor ({@code 1.0} = 100%}).
	 */
	default double getZoom() {
		return zoomProperty().get();
	}

	/**
	 * Returns the minimum zoom factor.
	 *
	 * @return the minimum zoom factor.
	 *
	 */
	public double getZoomMin();

	/**
	 * Returns the maximum zoom factor.
	 *
	 * @return the maximum zoom factor.
	 */
	public double getZoomMax();

	/**
	 * The content changed property. Each time this property changes its value,
	 * the zoomable content has been modified, thus signaling that observers
	 * need to update their view (or similar). Can also be used to fire a
	 * content changed event.
	 *
	 * @return the content changed property.
	 */
	public BooleanProperty contentChangedProperty();

	/**
	 * Fires a content changed event. Signals to observers that they need to
	 * update their view (or similar).
	 */
	default void fireContentChanged() {
		contentChangedProperty().set(!contentChangedProperty().get());
	}

	/**
	 * Returns the content pane. This is the unscaled pane with the content of
	 * the zoomable.
	 *
	 * @return the content pane.
	 */
	public Pane getContentPane();

	/**
	 * Checks whether the content pane is empty.
	 *
	 * @return {@code true} if the content pane is empty, {@code false}
	 * otherwise.
	 */
	public boolean isEmpty();

	/**
	 * The content bounds property. These are the unscaled bounds of the content
	 * pane.
	 *
	 * @return the content bounds property.
	 */
	public ReadOnlyObjectProperty<Bounds> contentBoundsProperty();

	/**
	 * Returns the content bounds. These are the unscaled bounds of the content
	 * pane.
	 *
	 * @return the content bounds.
	 */
	default Bounds getContentBounds() {
		return contentBoundsProperty().get();
	}

	/**
	 * Returns the scaled content bounds property. These are the scaled bounds
	 * (of the scaling pane) according to the current zoom factor.
	 *
	 * @return the scaled content bounds property.
	 */
	public ReadOnlyObjectProperty<Bounds> scaledContentBoundsProperty();

	/**
	 * Returns the scaled content bounds. These are the scaled bounds (of the
	 * scaling pane) according to the current zoom factor.
	 *
	 * @return the scaled content bounds.
	 */
	default Bounds getScaledContentBounds() {
		return scaledContentBoundsProperty().get();
	}

}
