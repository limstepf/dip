package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.api.imaging.rescaling.FxBresenham;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

/**
 * ZoomPane is a ScrollPane that can scale its contents.
 */
public class ZoomPane extends ScrollPane {

	private static double MIN_ZOOM_VALUE = 0; // 1 := 100%
	private static double MAX_ZOOM_VALUE = 4800;

	private final DipThreadPool threadPool;
	private final Scale scale = new Scale(1.0, 1.0);
	private final DoubleProperty zoom = new SimpleDoubleProperty(1.0);
	private final BooleanProperty contentProperty = new SimpleBooleanProperty();
	private final StackPane contentPane = new StackPane();
	private final StackPane scalingPane = new StackPane(contentPane);
	private final Group scalingGroup = new Group();

	/*
	 * So... this is a quite a painful hack to get around the fact that the
	 * JavaFX Prism pipeline does binilear interpolation, and nothing else.
	 * I.e. {@code setSmooth(false)} (e.g. on an {@code ImageView}) does
	 * nothing.
	 * Thus there is this image layer we're managing here, putting it on top of
	 * the scaled content group and do our resampling on our own. If we zoom in
	 * we actually do wanna see those darn pixels, right? Right.
	 * Once the JavaFX Prism pipeline can be told to just do NN interpolation,
	 * we can get rid of this. Hopefully soon enough.
	 * In the meantime, this is it, using the Bresenham algorithm (hard to beat)
	 * to do NN interpolation (with the unscaled content pane as source) to fill
	 * the viewport. And for upscaling only.
	 * The used algorithm is (according to my micro bench) equivalent to native
	 * awt scaling (setPixels) using the NN rendering hint. But we're doing it
	 * directly on a JavaFX WritableImage, to not have to convert to BufferImage
	 * and back. So that's pretty much as fast as it gets (unless parallelization
	 * would make sense here). Given this is all a temporary workaround, eh...
	 */
	private ImageView nnOverlay;
	private FxBresenham bresenham;
	private SnapshotParameters nnSnaphshotParameters;

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

		public static Interpolation get(String name) {
			try {
				return Interpolation.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return BILINEAR;
			}
		}

	}

	/**
	 * Creates a new ZoomPane.
	 *
	 * @param threadPool thread pool.
	 * @param nodes zooming content.
	 */
	public ZoomPane(DipThreadPool threadPool, Node... nodes) {
		getStyleClass().add("dip-zoom-pane");

		this.threadPool = threadPool;
		this.setMinWidth(0);
		this.setMinHeight(0);
		this.setMaxWidth(Double.MAX_VALUE);
		this.setMaxHeight(Double.MAX_VALUE);

		scalingGroup.getChildren().addAll(scalingPane);

		// there is a bug where scrollbars dont update to adapt to a
		// scaled group. So we don't scale the group itself, but some
		// extra pane instead... DUH.
		scalingPane.getTransforms().addAll(scale);
		setContent(scalingGroup);

		setZoomContent(nodes);

		zoom.addListener(zoomListener);
		contentProperty.addListener(contentListener);
		this.hvalueProperty().addListener(redrawListener);
		this.vvalueProperty().addListener(redrawListener);
	}

	private final ChangeListener<Number> zoomListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
		double s0 = oldValue.doubleValue();
		double s1 = newValue.doubleValue();

		// make sure we do not divide by zero
		if (s1 <= 0) {
			s1 = 0.00001;
		}

		if (s0 <= 0) {
			s0 = 0.00001;
		}

		/*
		 * changing the zoom shifts the image in the viewport in a rather
		 * unintuitive way. What we want is to look at the same center pixel
		 * (w.r.t. the viewport) before and after the adjustment. To do this
		 * we look at the center pixel before, and at a different center pixel
		 * after changing the zoom - both with respect to the unzoomed content
		 * pane, s.t. we can calculate the difference, scale back up to the new
		 * scale and adjust the slider positions accordingly.
		 */
		// calculate the position of the viewport's center pixel in the unzoomed
		// content pane before adjusting the scale. That's where we want to
		// 'look at' again afterwards.
		double invZoom = 1.0 / s0;
		double scrollX = scrollX();
		double scrollY = scrollY();
		Bounds zoomed = getContentBoundsZoomed();
		Bounds viewport = getViewportBounds();
		double scrollableWidth = zoomed.getWidth() - viewport.getWidth();
		double scrollableHeight = zoomed.getHeight() - viewport.getHeight();
		double scrollOffsetX = scrollX * scrollableWidth; // invZoom factored out
		double scrollOffsetY = scrollY * scrollableHeight;
		double srcWidth = zoomed.getWidth() - scrollableWidth; // invZoom factored out
		double srcHeight = zoomed.getHeight() - scrollableHeight;
		final double cx0 = invZoom * (scrollOffsetX + (srcWidth * 0.5));
		final double cy0 = invZoom * (scrollOffsetY + (srcHeight * 0.5));

		// adjust scale, which shifts the original center pixel, so...
		scale.setX(s1);
		scale.setY(s1);

		// calculate the position of the viewport's center pixel in the unzoomed
		// content pane after adjusting the scale.
		invZoom = 1.0 / s1;
		scrollX = scrollX();
		scrollY = scrollY();
		zoomed = getContentBoundsZoomed();
		viewport = getViewportBounds();
		scrollableWidth = zoomed.getWidth() - viewport.getWidth();
		scrollableHeight = zoomed.getHeight() - viewport.getHeight();
		scrollOffsetX = scrollX * scrollableWidth; // invZoom factored out
		scrollOffsetY = scrollY * scrollableHeight;
		srcWidth = zoomed.getWidth() - scrollableWidth; // invZoom factored out
		srcHeight = zoomed.getHeight() - scrollableHeight;
		final double cx1 = invZoom * (scrollOffsetX + (srcWidth * 0.5));
		final double cy1 = invZoom * (scrollOffsetY + (srcHeight * 0.5));

		// This pixel/position is not the one we had centered before, so calculate
		// the difference (on the unzoomed content pane), scale to zoomed content
		// pane coordinates, and adjust the slider positions.
		final double dx = (cx1 - cx0) * s1; // dx/dy in pixel distance, upscaled
		final double dy = (cy1 - cy0) * s1; // to new zoom level

		suppressRedrawListener = true;
		if (scrollableWidth > 0) {
			final double hx = dx * scrollRangeX() / scrollableWidth;
			this.setHvalue(getHvalue() - hx);
		}
		if (scrollableHeight > 0) {
			final double hy = dy * scrollRangeY() / scrollableHeight;
			this.setVvalue(getVvalue() - hy);
		}
		suppressRedrawListener = false;

		invalidate(zoomed, viewport);
	};

	private final InvalidationListener contentListener = (e) -> invalidate(
			getContentBoundsZoomed(),
			getViewportBounds()
	);
	private boolean suppressRedrawListener = false;
	private final InvalidationListener redrawListener = (e) -> updateNN();

	/**
	 * Updates the viewport.
	 *
	 * @param zoomed zommed content bounds.
	 * @param viewport viewport bounds.
	 */
	private void invalidate(Bounds zoomed, Bounds viewport) {
		updateScalingGroupOffset(zoomed, viewport);
		updateNN();
	}

	/**
	 * Updates the position of the viewport. Updates the {@code hvalueProperty}
	 * and the {@code vvalueProperty) at the same time, triggering only a single zoom pane
	 * redraw listener (as opposed to setting both properties individually).
	 *
	 * @param hpos the hvalue of the viewport (or the zoompane's slider).
	 * @param vpos the vvalue of the viewport (or the zoompane's slider).
	 */
	public void updateViewportPosition(double hpos, double vpos) {
		suppressRedrawListener = true;
		hvalueProperty().setValue(hpos);
		vvalueProperty().setValue(vpos);
		suppressRedrawListener = false;
		updateNN();
	}

	/**
	 * Updates the offset of the scaling group to center content that is smaller
	 * than the viewport itself.
	 *
	 * @param zoomed zommed content bounds.
	 * @param viewport viewport bounds.
	 */
	private void updateScalingGroupOffset(Bounds zoomed, Bounds viewport) {
		// center zommed content pane in viewport if it's smaller than the
		// viewport itself
		final double offsetX = (zoomed.getWidth() < viewport.getWidth())
				? (viewport.getWidth() - zoomed.getWidth()) * .5
				: 0;
		final double offsetY = (zoomed.getHeight() < viewport.getHeight())
				? (viewport.getHeight() - zoomed.getHeight()) * .5
				: 0;

		scalingGroup.setTranslateX(offsetX);
		scalingGroup.setTranslateY(offsetY);
	}

	/**
	 * Manually resamples from non-zoomed content pane.
	 */
	private void updateNN() {
		if (suppressRedrawListener || !getInterpolation().equals(Interpolation.NEAREST_NEIGHBOR)) {
			return;
		}

		// only do NN resampling for upscaling
		if (getZoom() <= 1.0) {
			nnOverlay.setImage(null);
			nnOverlay.setVisible(false);
			return;
		} else {
			nnOverlay.setVisible(true);
		}

		final double invZoom = 1.0 / getZoom();
		final double scrollOffsetX = scrollOffsetX();
		final double scrollOffsetY = scrollOffsetY();
		final double srcOffsetX = invZoom * scrollOffsetX;
		final double srcOffsetY = invZoom * scrollOffsetY;
		final Bounds zoomed = getContentBoundsZoomed();
		double srcWidth = invZoom * (zoomed.getWidth() - scrollableWidth());
		double srcHeight = invZoom * (zoomed.getHeight() - scrollableHeight());

		final Bounds content = getContentBounds();
		final double contentWidth = content.getWidth();
		final double contentHeight = content.getHeight();

		if (srcWidth <= 0) {
			srcWidth = contentWidth;
		} else if ((srcOffsetX + srcWidth) > contentWidth) {
			srcWidth = contentWidth - srcOffsetX;
		}
		if (srcHeight <= 0) {
			srcHeight = contentHeight;
		} else if ((srcOffsetY + srcHeight) > contentHeight) {
			srcHeight = contentHeight - srcOffsetY;
		}
		final int dstWidth = (int) (getZoom() * srcWidth);
		final int dstHeight = (int) (getZoom() * srcHeight);

		if (dstWidth <= 0 || dstHeight <= 0) {
			this.nnOverlay.setImage(null);
			return;
		}

		final Rectangle2D region = new Rectangle2D(
				srcOffsetX,
				srcOffsetY,
				srcWidth,
				srcHeight
		);

		// the snapshot needs to be taken on the Java FX application thread
		this.nnSnaphshotParameters.setViewport(region);
		final WritableImage src = snapshot(nnSnaphshotParameters);

		// already set the scroll offset now (instead of in the runLater block
		// below) to reduce/eliminate "screen tearing/jerking". Also RedrawListener
		// needs to be suppressed to prevent a stack overflow.
		suppressRedrawListener = true;
		nnOverlay.setLayoutX(scrollOffsetX);
		nnOverlay.setLayoutY(scrollOffsetY);
		suppressRedrawListener = false;

		// so... the problem here was that we tried to reuse the dst image, resulting
		// in writing to it from a worker thread, while it is currently part of the
		// javafx scene graph (and should only be modified there).
		// the current solution is to make bresenham always create a new image
		// so lots of mem goes quickly down the drain and needs to be gc'd.
		// maybe we could double buffer this or something (but should we?).
		// Other than that, modern GC is pretty good at handling this: the app.
		// takes all memory it can very quickly, and then just does GC every so
		// often, which isn't really that bad...
		final Runnable run = () -> {
			final WritableImage dst = bresenham.zoom(src, dstWidth, dstHeight);
			// So here's the thing: we have a lovely race condition here, where either
			// bresenham can be called again before runLater attaches the image to the
			// scene graph, or another where the runLater of a newer dst image gets
			// replaced by an older one.
			// Anyways, as is, this doesn't seem to be much of a problem, and is
			// inherently safe, since we never touch an image again, once attached to
			// the scene graph. However, I've tried to double buffer the dst image,
			// and flip (if safe to do so) after a call to bresenham. Of course this
			// all needs to be synchronized now (bresenham calculation and retrieval
			// of image, which allowes to safely flip the buffer) and... well, things
			// just got worse (multithreading is hard, eh?).
			// Thus, considering this is a workaround until we can have native NN in
			// the JavaFX Prism pipeline: screw this, and just do some more GC then.
			// If this bugs you, feel free to try again. Extra points for also reusing
			// the src images to grab the snapshots (they need to be taken on the Java
			// FX application thread). :)
			Platform.runLater(() -> {
				nnOverlay.setImage(dst);
				updateScalingGroupOffset(getContentBoundsZoomed(), getViewportBounds());
			});
		};
		threadPool.getExecutorService().submit(run);
	}

	private final ObjectProperty<Interpolation> interpolationProperty = new SimpleObjectProperty(Interpolation.BILINEAR) {
		@Override
		protected void invalidated() {
			final Interpolation ip = (Interpolation) get();
			switch (ip) {
				case NEAREST_NEIGHBOR:
					// in future this can hopefully be replaced by a simple:
					// scalingPane.setSmooth(false)
					bresenham = new FxBresenham();
					nnSnaphshotParameters = new SnapshotParameters();
					nnOverlay = new ImageView();
					nnOverlay.setMouseTransparent(true);
					scalingGroup.getChildren().add(nnOverlay);
					break;

				case BILINEAR:
					// ... and here a scalingPane.setSmooth(true)
					scalingGroup.getChildren().remove(nnOverlay);
					nnOverlay = null;
					nnSnaphshotParameters = null;
					bresenham = null;
					break;
			}
			updateNN();
		}
	};

	/**
	 * The interpolation property. Used for zooming/resampling.
	 *
	 * @return the interpolation property.
	 */
	public ObjectProperty<Interpolation> interpolationProperty() {
		return interpolationProperty;
	}

	/**
	 * Returns the interpolation type used for zooming/resampling.
	 *
	 * @return the interpolation type.
	 */
	public Interpolation getInterpolation() {
		return interpolationProperty().get();
	}

	/**
	 * Sets the interpolation type used for zooming/resampling.
	 *
	 * @param type the interpolation type.
	 */
	public void setInterpolation(Interpolation type) {
		interpolationProperty().set(type);
	}

	/**
	 * Computes the scrollable width. The scrollable width is the amount that
	 * can be scrolled, or in other words the amount of the image that curently
	 * is not shown/visible in the viewport. If the viewport is larger than the
	 * (zoomed) pane then the scrollable width is negative, and divided by two
	 * the margin on the left and right side (assuming the pane is centered in
	 * the viewport).
	 *
	 * <p>
	 * Thus, unless the scrollable width is negative, the width of the zoomed
	 * pane minus the scrollable width equals the shown/visible width of the
	 * zoomed pane.
	 *
	 * @return the scrollable width, or the free width if negative.
	 */
	public double scrollableWidth() {
		return getContentBoundsZoomed().getWidth() - getViewportBounds().getWidth();
	}

	/**
	 * Computes the scroll X range. This is usually 1.0 with hmin = 0, and hmax
	 * = 1. But not necessarily.
	 *
	 * @return the scroll X range.
	 */
	public double scrollRangeX() {
		return getHmax() - getHmin();
	}

	/**
	 * Computes the scroll X position.
	 *
	 * @return the scroll X position (in 0..1).
	 */
	public double scrollX() {
		return getHvalue() * scrollRangeX();
	}

	/**
	 * Computes the scroll X offset. This offset is 0 if the scroll position is
	 * 0 (i.e. fully left), and equals {@code scrollableWidth} if panned fully
	 * to the right. Note that {@code scrollableWidth()} needs to be positive to
	 * get a correct answer.
	 *
	 * @return the scroll X offset (in 0..{@code scrollableWidth()}).
	 */
	public double scrollOffsetX() {
		return scrollX() * scrollableWidth();
	}

	/**
	 * Computes the scrollable height.
	 *
	 * @return the scrollabel height, or the free height if negative.
	 */
	public double scrollableHeight() {
		return getContentBoundsZoomed().getHeight() - getViewportBounds().getHeight();
	}

	/**
	 * Computes the scroll Y range. This is usually 1.0 with vmin = 0, and vmax
	 * = 1. But not necessarily.
	 *
	 * @return the scroll X range.
	 */
	public double scrollRangeY() {
		return getVmax() - getVmin();
	}

	/**
	 * Computes the scroll Y position.
	 *
	 * @return the scroll Y position (in 0..1).
	 */
	public double scrollY() {
		return getVvalue() * scrollRangeY();
	}

	/**
	 * Computes the scroll Y offset.
	 *
	 * @return the scroll Y offset (in 0..{@code scrollableHeight()}).
	 */
	public double scrollOffsetY() {
		return scrollY() * scrollableHeight();
	}

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

	/**
	 * Returns the minimum zoom value.
	 *
	 * @return the minimum zoom value (1 equals 100%).
	 */
	public double minZoomValue() {
		return MIN_ZOOM_VALUE;
	}

	/**
	 * Returns the maximum zoom value.
	 *
	 * @return the maximum zoom value (1 equals 100%).
	 */
	public double maxZoomValue() {
		return MAX_ZOOM_VALUE;
	}

}
