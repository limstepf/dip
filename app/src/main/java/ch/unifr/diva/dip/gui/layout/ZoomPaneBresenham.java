package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.imaging.rescaling.FxBresenham;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 * A zoom pane with an overlay and the option to resample the content with
 * nearest neighbor (NN) interpolation.
 *
 * <p>
 * This zoom pane is pretty much an ugly hack/workaround due to the fact that
 * the JavaFX Prism pipeline supports nothing but bilinear interpolation. Once
 * {@code setSmooth(false)} (or something similar) actually does the trick this
 * zoom pane should be deprecated.
 */
public class ZoomPaneBresenham extends ZoomPaneWithOverlay {

	/*
	 * local scene graph:
	 * ------------------
	 *
	 *                         scrollPane
	 *                              |
	 *                              /
	 *                           muxPane
	 *                              |
	 *          --------------------------------------------
	 *          |                   |                      |
	 *          /                   /                      /
	 *     paddedRegion       nnScalingView         overlayGroup -> (CLIP)
	 *          |                   \                      |
	 *          /                   |                      /
	 *     scrollGroup           snapshot         overlayScalingPane -> (SCALE)
	 *          |                   \                      |
	 *          /                   |                      /
	 *     scalingPane -> (SCALE)   |             overlayContentPane
	 *          |                   |
	 *          /                   |
	 *     contentPane -> ----------|
	 *
	 *
	 * The same scale transform (SCALE) is applied to both: the scalingPane and
	 * the overlayScalingPane. The overlayGroup is clipped (CLIP) by the bounds
	 * of the paddedRegion.
	 * The nnScalingView takes a manual snapshot of the contentPane and manually
	 * scales the image (up) with the bresenham algorithm.
	 */
	protected final DipThreadPool threadPool;
	protected ImageView nnScalingView;
	protected SnapshotParameters nnSnaphshotParameters;
	protected FxBresenham bresenham;

	/**
	 * Creates a new zoom pane with an overlay and NN interpolation.
	 *
	 * @param threadPool the thread pool. This should be a discarding thread
	 * pool with a single thread.
	 */
	public ZoomPaneBresenham(DipThreadPool threadPool) {
		this(threadPool, DEFAULT_MIN_ZOOM, DEFAULT_MAX_ZOOM);
	}

	/**
	 * Creates a new zoom pane with an overlay and NN interpolation.
	 *
	 * @param threadPool the thread pool. This should be a discarding thread
	 * pool with a single thread.
	 * @param zoomMin the minimum zoom factor.
	 * @param zoomMax the maximum zoom factor.
	 */
	public ZoomPaneBresenham(DipThreadPool threadPool, double zoomMin, double zoomMax) {
		super(zoomMin, zoomMax);
		this.threadPool = threadPool;

		this.contentChangedProperty.addListener(contentListener);
	}

	/**
	 * The interpolation property. Used for zooming/resampling.
	 *
	 * @return the interpolation property.
	 */
	public final ObjectProperty<Zoomable.Interpolation> interpolationProperty() {
		return interpolationProperty;
	}

	/**
	 * Returns the interpolation method used for zooming/resampling.
	 *
	 * @return the interpolation method.
	 */
	public final Zoomable.Interpolation getInterpolation() {
		return interpolationProperty().get();
	}

	/**
	 * Sets the interpolation method used for zooming/resampling.
	 *
	 * @param method the interpolation method.
	 */
	public final void setInterpolation(Zoomable.Interpolation method) {
		interpolationProperty().set(method);
	}

	protected final ObjectProperty<Zoomable.Interpolation> interpolationProperty = new SimpleObjectProperty<Zoomable.Interpolation>() {
		@Override
		public void set(Zoomable.Interpolation method) {
			switch (method) {
				case NEAREST_NEIGHBOR:
					// in future this can hopefully be replaced by a simple:
					// scalingPane.setSmooth(false)
					bresenham = new FxBresenham();
					nnSnaphshotParameters = new SnapshotParameters();
					nnScalingView = new ImageView();
					nnScalingView.setMouseTransparent(true);
					muxPane.getChildren().setAll(
							paddedRegion,
							nnScalingView,
							overlayGroup
					);
					repaintNN();
					break;

				case BILINEAR:
					muxPane.getChildren().setAll(
							paddedRegion,
							overlayGroup
					);
					nnScalingView = null;
					nnSnaphshotParameters = null;
					bresenham = null;
					break;
			}
			super.set(method);
		}
	};

	protected InvalidationListener contentListener = (e) -> onContentChanged();

	protected void onContentChanged() {
		if (changeIsLocal) {
			return;
		}
		repaintNN();
	}

	/*
	 * there is no need to overwrite onViewportBounds to repaintNN since it
	 * already will fire a contentChanged event
	 */
	@Override
	protected void onZoom() {
		super.onZoom();
		if (changeIsLocal) {
			return;
		}
		repaintNN();
	}

	@Override
	protected void onPanning() {
		super.onPanning();
		if (changeIsLocal) {
			return;
		}
		repaintNN();
	}

	protected void repaintNN() {
		if (changeIsLocal || !Interpolation.NEAREST_NEIGHBOR.equals(getInterpolation())) {
			return;
		}

		// only do NN resampling for upscaling
		if (getZoom() <= 1.0) {
			nnScalingView.setImage(null);
			nnScalingView.setVisible(false);
			return;
		}

		final VisibleRegion visibleRegion = getVisibleRegion();
		if (visibleRegion.isEmpty()) {
			nnScalingView.setImage(null);
			nnScalingView.setVisible(false);
			return;
		}

		final Pannable.SubpixelRectangle2D sourceRegion = visibleRegion.getUnscaledVisibleSubpixelRegion();
		nnSnaphshotParameters.setViewport(sourceRegion.getRectangle2D());
		final WritableImage src = contentPane.snapshot(nnSnaphshotParameters, null);

		/*
		 * setting the offset here already (instead of in the runLater block below)
		 * give's us smooth zooming; otherwise we'd see rather heavy "screen tearing".
		 */
		nnScalingView.setLayoutX(visibleRegion.getPaddedOffsetX());
		nnScalingView.setLayoutY(visibleRegion.getPaddedOffsetY());

		final Runnable run = () -> {
			/*
			 * Here be dragons. And race conditions (hence the use of a discarding
			 * thread pool). Trying to double-buffer and flip between two dst
			 * images (which needs to be synchronized) didn't help at all, so we
			 * just keep creating new images and throw 'em away shortly after.
			 * GC is a good boy. And yes, the snapshot for the src image needs
			 * to be taken on the JavaFX application thread.
			 */
			final WritableImage dst = bresenham.zoom(
					src,
					(int) visibleRegion.getScaledWidth(),
					(int) visibleRegion.getScaledHeight(),
					sourceRegion.shiftX,
					sourceRegion.restX,
					sourceRegion.shiftY,
					sourceRegion.restY
			);

			Platform.runLater(() -> {
				nnScalingView.setImage(dst);
				nnScalingView.setVisible(true);
				muxPane.layout();
			});
		};
		threadPool.getExecutorService().submit(run);
	}

}
