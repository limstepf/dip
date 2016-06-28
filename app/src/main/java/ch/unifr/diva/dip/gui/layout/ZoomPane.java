package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.utils.FxUtils;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

/**
 * ZoomPane is a ScrollPane that can scale its contents.
 */
public class ZoomPane extends ScrollPane {

	private final Scale scale = new Scale(1.0, 1.0);
	private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);
	private final BooleanProperty contentProperty = new SimpleBooleanProperty();
	private final StackPane contentPane = new StackPane();
	private final StackPane scalingPane = new StackPane(contentPane);
	private final Group scalingGroup = new Group(scalingPane);

	/**
	 * Default constructor.
	 *
	 * @param nodes zooming content.
	 */
	public ZoomPane(Node... nodes) {
		getStyleClass().add("dip-zoom-pane");

		this.setMinWidth(0);
		this.setMinHeight(0);
		this.setMaxWidth(Double.MAX_VALUE);
		this.setMaxHeight(Double.MAX_VALUE);

		// there is a bug where scrollbars dont update to adapt to a
		// scaled group. So we don't scale the group itself, but some
		// extra pane instead... DUH.
		scalingPane.getTransforms().addAll(scale);
		setContent(scalingGroup);

		setZoomContent(nodes);

		zoom.addListener(zoomListener);
	}

	private final ChangeListener<Number> zoomListener = (
			ObservableValue<? extends Number> observable,
			Number oldValue,
			Number newValue) -> {
				scale.setX(newValue.doubleValue());
				scale.setY(newValue.doubleValue());
				requestLayout();

				// TODO: re-center/adjust scrollers in case z > v
				Bounds z = getContentBoundsZoomed();
				Bounds v = this.getViewportBounds();
				double offsetX = (z.getWidth() < v.getWidth())
						? (v.getWidth() - z.getWidth()) * .5
						: 0;
				double offsetY = (z.getHeight() < v.getHeight())
						? (v.getHeight() - z.getHeight()) * .5
						: 0;

				scalingGroup.setTranslateX(offsetX);
				scalingGroup.setTranslateY(offsetY);
			};

	/**
	 * Sets the zooming content.
	 *
	 * @param nodes the content to be zoomed.
	 */
	public final void setZoomContent(Node... nodes) {
		contentPane.getChildren().setAll(nodes);

		double z = zoom.get();
		zoomListener.changed(zoom, z, z);
	}

	/**
	 * Checks whether the ZoomPane has some content set.
	 *
	 * @return True if there is some zooming content, False otherwise.
	 */
	public boolean hasZoomContent() {
		return !contentPane.getChildren().isEmpty();
	}

	/**
	 * Returns the (unscaled/unzoomed) bounds of the zooming content.
	 *
	 * @return original bounds of the zooming content.
	 */
	public final Bounds getContentBounds() {
		return contentPane.getBoundsInLocal();
	}

	/**
	 * Returns the scaled/zoomed bounds of the zooming content.
	 *
	 * @return scaled/zoomed bounds of the zooming content.
	 */
	public final Bounds getContentBoundsZoomed() {
		return scalingGroup.getBoundsInParent();
	}

	/**
	 * Returns a snapshot of the canvas.
	 *
	 * @param parameters snapshot parameters.
	 * @return A WritableImage.
	 */
	public final WritableImage snapshot(SnapshotParameters parameters) {
		return contentPane.snapshot(parameters, null);
	}

	/**
	 * Modifies the zoomContentProperty in order to fire a "content changed"
	 * event to all subscribed listeners.
	 */
	public void fireContentChange() {
		contentProperty.set(!contentProperty.get());
	}

	/**
	 * The zoom content property. This property changes whenever the content of
	 * the zoom pane have change.
	 *
	 * @return the zoom content property.
	 */
	public BooleanProperty zoomContentProperty() {
		return contentProperty;
	}

	/**
	 * Returns the zoom factor of the content in the ZoomPane.
	 *
	 * @return the zoom factor.
	 */
	public final double getZoom() {
		return this.zoom.doubleValue();
	}

	/**
	 * Sets the zoom factor of the content in the ZoomPane.
	 *
	 * @param zoom the zoom factor.
	 */
	public final void setZoom(double zoom) {
		this.zoom.set(zoom);
	}

	/**
	 * The zoom factor property.
	 *
	 * @return the zoom factor property.
	 */
	public final DoubleProperty zoomProperty() {
		return this.zoom;
	}

	// listeners for: bindMinDimensions/unbindMinDimensions
	private final Map<Region, ChangeListener> minDimListeners = new HashMap<>();

	/**
	 * Binds minWidth and minHeight properties of a region to the size of the
	 * ZoomPane's viewport.
	 *
	 * @param region the region to set minWidth and minHeight to match the
	 * viewport.
	 */
	public void bindMinDimensions(Region region) {
		final ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
			final Bounds bounds = (Bounds) newValue;
			region.setMinWidth(bounds.getWidth());
			region.setMinHeight(bounds.getHeight());
		};
		this.minDimListeners.put(region, listener);
		this.viewportBoundsProperty().addListener(listener);
	}

	/**
	 * Unbinds a region's minWidth and maxHeight from listening to the viewport.
	 *
	 * @param region the region to unbind.
	 */
	public void unbindMinDimensions(Region region) {
		final ChangeListener listener = this.minDimListeners.remove(region);
		if (listener != null) {
			this.viewportBoundsProperty().removeListener(listener);
		}
	}
}
