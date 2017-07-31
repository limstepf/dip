package ch.unifr.diva.dip.gui.layout;

import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * A zoom pane with an overlay. The overlay pane is scaled like and aligned with
 * the content pane, and placed on top of anything else. Nodes in the overlay
 * pane may project even beyond the padded region of the zoom pane, but are
 * clipped by it.
 */
public class ZoomPaneWithOverlay extends ZoomPaneSimple {

	/*
	 * local scene graph:
	 * ------------------
	 *
	 *                       scrollPane
	 *                            |
	 *                            /
	 *                         muxPane
	 *                            |
	 *            ---------------------------------------
	 *            |                                     |
	 *            /                                     /
	 *       paddedRegion                         overlayGroup -> (CLIP)
	 *            |                                     |
	 *            /                                     /
	 *       scrollGroup                        overlayScalingPane -> (SCALE)
	 *            |                                     |
	 *            /                                     /
	 *       scalingPane -> (SCALE)             overlayContentPane
	 *            |
	 *            /
	 *       contentPane
	 *
	 *
	 * The same scale transform (SCALE) is applied to both: the scalingPane and
	 * the overlayScalingPane. The overlayGroup is clipped (CLIP) by the bounds
	 * of the paddedRegion.
	 */
	protected final Pane muxPane;
	protected final Group overlayGroup;
	protected final Rectangle clipRect;
	protected final Pane overlayScalingPane;
	protected final Pane overlayContentPane;

	/**
	 * Creates a new zoom pane with an overlay.
	 */
	public ZoomPaneWithOverlay() {
		this(DEFAULT_MIN_ZOOM, DEFAULT_MAX_ZOOM);
	}

	/**
	 * Creates a new zoom pane with an overlay.
	 *
	 * @param zoomMin the minimum zoom factor.
	 * @param zoomMax the maximum zoom factor.
	 */
	public ZoomPaneWithOverlay(double zoomMin, double zoomMax) {
		super(zoomMin, zoomMax);

		this.overlayContentPane = new Pane();
		this.overlayScalingPane = new Pane(overlayContentPane);
		overlayScalingPane.getTransforms().addAll(scale);
		overlayScalingPane.setMouseTransparent(true);
		overlayScalingPane.setManaged(false);

		this.clipRect = new Rectangle();
		clipRect.widthProperty().bind(paddedRegion.widthProperty());
		clipRect.heightProperty().bind(paddedRegion.heightProperty());
		clipRect.layoutXProperty().bind(
				Bindings.multiply(paddedRegion.leftPaddingRealProperty(), -1)
		);
		clipRect.layoutYProperty().bind(
				Bindings.multiply(paddedRegion.topPaddingRealProperty(), -1)
		);

		this.overlayGroup = new Group(overlayScalingPane);
		overlayGroup.setManaged(false);
		overlayGroup.setClip(clipRect);
		overlayGroup.layoutXProperty().bind(
				paddedRegion.leftPaddingRealProperty()
		);
		overlayGroup.layoutYProperty().bind(
				paddedRegion.topPaddingRealProperty()
		);

		/*
		 * A default Pane will bug out sooner* or later, s.t. we end up with way
		 * too much width/height, screwing up the ScrollPane and NavigatorWidget...
		 * Not really sure why that is (or if it's even supposed to work like this).
		 * Anyways: overwriting computePrefWidth/-Height fixes this problem.
		 *
		 * (*) to reproduce: use a plain Pane as muxPane, then start the app with
		 * some large enough page, zoom to 555% (super large), and back to 35%
		 * (small, s.t. only vertical scroll is available, while the full width of
		 * the page is visible. Note how the width is far too large, and there's
		 * also a horizontal scrollbar to scroll into the empty space...
		 */
		this.muxPane = new Pane() {
			@Override
			protected double computeMinWidth(double height) {
				return paddedRegion.computeMinWidth(height);
			}

			@Override
			protected double computeMinHeight(double width) {
				return paddedRegion.computeMinHeight(width);
			}

			@Override
			protected double computePrefWidth(double height) {
				return paddedRegion.computePrefWidth(height);
			}

			@Override
			protected double computePrefHeight(double width) {
				return paddedRegion.computePrefHeight(width);
			}
		};

		muxPane.getChildren().setAll(
				paddedRegion,
				overlayGroup
		);
		scrollPane.setContent(muxPane);
	}

	/**
	 * Sets the content of the zoom pane overlay.
	 *
	 * @param nodes the content of the overlay.
	 */
	public void setOverlayContent(Node... nodes) {
		this.overlayContentPane.getChildren().setAll(nodes);
		fireContentChange();
	}

	/**
	 * Clears the content of the zoom pane overlay.
	 */
	public void clearOverlayContent() {
		this.overlayContentPane.getChildren().clear();
		fireContentChange();
	}

	/**
	 * Check whether the overlay pane is empty.
	 *
	 * @return {@code true} if the overlay pane is empty, {@code false}
	 * otherwise.
	 */
	public boolean isOverlayEmpty() {
		return this.overlayContentPane.getChildren().isEmpty();
	}

}
