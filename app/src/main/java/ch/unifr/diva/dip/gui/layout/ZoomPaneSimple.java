package ch.unifr.diva.dip.gui.layout;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/**
 * A simple zoom pane. Comes with a single content pane, optional padding and
 * bilinear interpolation (only).
 */
public class ZoomPaneSimple implements Pannable {

	protected final static double DEFAULT_MIN_ZOOM = 0; // 1 := 100%
	protected final static double DEFAULT_MAX_ZOOM = 4800;

	protected final DoubleProperty zoom;
	protected final double zoomMin;
	protected final double zoomMax;
	protected final Scale scale;

	/*
	 * local scene graph:
	 * ------------------
	 *
	 *                       scrollPane
	 *                            |
	 *                            /
	 *                       paddedRegion
	 *                            |
	 *                            /
	 *                       scrollGroup
	 *                            |
	 *                            /
	 *                       scalingPane -> scale transform
	 *                            |
	 *                            /
	 *                       contentPane
	 */
	protected final ScrollPane scrollPane;
	protected final PaddedRegion paddedRegion;
	protected final Group scrollGroup;
	protected final Pane contentPane;
	protected final Pane scalingPane;

	protected final DoubleProperty hminRealProperty;
	protected final DoubleProperty hmaxRealProperty;
	protected final DoubleProperty vminRealProperty;
	protected final DoubleProperty vmaxRealProperty;

	protected final BooleanProperty contentChangedProperty;
	protected boolean changeIsLocal;

	/**
	 * Creates a new simple zoom pane.
	 */
	public ZoomPaneSimple() {
		this(DEFAULT_MIN_ZOOM, DEFAULT_MAX_ZOOM);
	}

	/**
	 * Creates a new simple zoom pane.
	 *
	 * @param zoomMin the minimum zoom factor.
	 * @param zoomMax the maximum zoom factor.
	 */
	public ZoomPaneSimple(double zoomMin, double zoomMax) {
		this.zoom = new SimpleDoubleProperty(1.0);
		zoom.addListener(zoomListener);
		this.zoomMin = zoomMin;
		this.zoomMax = zoomMax;
		this.scale = new Scale(1.0, 1.0);

		this.contentChangedProperty = new SimpleBooleanProperty();

		this.contentPane = new Pane();
		this.scalingPane = new Pane(contentPane);
		scalingPane.getTransforms().addAll(scale);

		this.scrollGroup = new Group(scalingPane);
		this.paddedRegion = new PaddedRegion(scrollGroup);

		this.scrollPane = new ScrollPane();
		scrollPane.getStyleClass().add("dip-zoom-pane");
		scrollPane.setMinWidth(0);
		scrollPane.setMinHeight(0);
		scrollPane.setMaxWidth(Double.MAX_VALUE);
		scrollPane.setMaxHeight(Double.MAX_VALUE);
		scrollPane.setBackground(Background.EMPTY);
		scrollPane.setContent(paddedRegion);

		this.hminRealProperty = new SimpleDoubleProperty(hminProperty().get());
		this.hmaxRealProperty = new SimpleDoubleProperty(hmaxProperty().get());
		this.vminRealProperty = new SimpleDoubleProperty(vminProperty().get());
		this.vmaxRealProperty = new SimpleDoubleProperty(vmaxProperty().get());

		scrollPane.hvalueProperty().addListener(panningListener);
		scrollPane.vvalueProperty().addListener(panningListener);
		scrollPane.viewportBoundsProperty().addListener(viewportBoundsListener);

		paddedRegion.setRedispatchTarget(contentPane);
		paddedRegion.minBoundsProperty().bind(scrollPane.viewportBoundsProperty());
	}

	protected DoubleProperty paddingBufferProperty;
	protected final static double DEFAULT_PADDING_BUFFER = 100;

	/**
	 * The padding buffer (or reserve) property. The padding buffer is the
	 * amount of the panned content that can not be scrolled outside the
	 * viewport, or if negative the amount that can be panned s.t. the content
	 * isn't visible any longer.
	 *
	 * @return the padding buffer property.
	 */
	public DoubleProperty paddingBufferProperty() {
		if (paddingBufferProperty == null) {
			paddingBufferProperty = new SimpleDoubleProperty(DEFAULT_PADDING_BUFFER) {
				@Override
				public void invalidated() {
					paddedRegion.requestLayout();
				}
			};
		}
		return paddingBufferProperty;
	}

	/**
	 * Returns the padding buffer.
	 *
	 * @return the current padding buffer.
	 */
	public double getPaddingBuffer() {
		if (paddingBufferProperty == null) {
			return DEFAULT_PADDING_BUFFER;
		}
		return paddingBufferProperty().get();
	}

	/**
	 * Sets the padding buffer.
	 *
	 * @param value the padding buffer.
	 */
	public void setPaddingBuffer(double value) {
		paddingBufferProperty().set(value);
	}

	/**
	 * The padding property. Padding method used to set the padding around the
	 * panned content.
	 */
	protected ObjectProperty<Pannable.Padding> paddingMethodProperty = new SimpleObjectProperty<Pannable.Padding>(Pannable.Padding.NONE) {
		@Override
		public void invalidated() {
			updatePadding();
		}
	};

	/**
	 * The padding method property.
	 *
	 * @return the padding method property.
	 */
	public ObjectProperty<Pannable.Padding> paddingMethodProperty() {
		return paddingMethodProperty;
	}

	/**
	 * Sets the padding method.
	 *
	 * @param method the padding method.
	 */
	public void setPaddingMethod(Pannable.Padding method) {
		paddingMethodProperty().set(method);
	}

	/**
	 * Returns the padding method.
	 *
	 * @return the current padding method.
	 */
	public Pannable.Padding getPaddingMethod() {
		return paddingMethodProperty().get();
	}

	protected void updatePadding() {
		switch (getPaddingMethod()) {
			case VIEWPORT:
				final Bounds viewportBounds = getViewportBounds();
				final Bounds scaledBounds = getScaledContentBounds();
				final double verticalPadding;
				final double horizontalPadding;
				// effective padding is either viewport size - buffer size,
				// where the buffer size is the part of the content still visible
				// (if positive), or 0 s.t. minimum bounds will center the content
				if (scaledBounds.getWidth() <= viewportBounds.getWidth()) {
					horizontalPadding = 0;
					setHminReal(getHmin());
					setHmaxReal(getHmax());
				} else {
					horizontalPadding = Math.max(
							viewportBounds.getWidth() - getPaddingBuffer(),
							0
					);
					// setHminReal/setHmaxReal below with horizontalPadding > 0
					// padding needs to be set first to get real padding
				}
				if (scaledBounds.getHeight() <= viewportBounds.getHeight()) {
					verticalPadding = 0;
					setVminReal(getVmin());
					setVmaxReal(getVmax());
				} else {
					verticalPadding = Math.max(
							viewportBounds.getHeight() - getPaddingBuffer(),
							0
					);
					// setVminReal/setVmaxReal below with verticalPadding > 0
					// padding needs to be set first to get real padding
				}
				setPadding(verticalPadding, horizontalPadding);

				if (horizontalPadding > 0) {
					final double rangeX = scrollRangeX();
					final double left = paddedRegion.getLeftPaddingReal();
					final double right = paddedRegion.getRightPaddingReal();
					final double width = left + scaledBounds.getWidth() + right;
					setHminReal(left * rangeX / width);
					setHmaxReal(getHmax() - (right * rangeX / width));
				}

				if (verticalPadding > 0) {
					final double rangeY = scrollRangeY();
					final double top = paddedRegion.getTopPaddingReal();
					final double bottom = paddedRegion.getBottomPaddingReal();
					final double height = top + scaledBounds.getHeight() + bottom;
					setVminReal(top * rangeY / height);
					setVmaxReal(getVmax() - (bottom * rangeY / height));
				}
				break;

			case NONE:
			default:
				setPadding(0, 0);
				setHminReal(getHmin());
				setHmaxReal(getHmax());
				setVminReal(getVmin());
				setVmaxReal(getVmax());
				break;
		}
		fireContentChange();
	}

	protected void setPadding(double vertical, double horizontal) {
		this.paddedRegion.setPadding(vertical, horizontal);
	}

	private final InvalidationListener viewportBoundsListener = (e) -> onViewportBounds();

	protected void onViewportBounds() {
		updatePadding();
	}

	protected final InvalidationListener zoomListener = (e) -> onZoom();
	protected final InvalidationListener panningListener = (e) -> onPanning();
	protected double lastS;
	protected Point2D lastCenterPixel;

	protected void onZoom() {
		final double s1 = this.zoom.get();

		if (lastCenterPixel == null) {
			lastS = s1;
			lastCenterPixel = getCenterPixel();
		}

		/*
		 * the changeIsLocal flag and the call to layout are needed to not fire
		 * the panning listener here (or zooming on the same pixel will bug out
		 */
		changeIsLocal = true;
		scale.setX(s1);
		scale.setY(s1);
		scrollPane.layout();
		changeIsLocal = false;

		moveToPixel(lastCenterPixel);

		lastS = s1;
		lastCenterPixel = getCenterPixel();
	}

	protected void onPanning() {
		if (changeIsLocal) {
			return;
		}
		lastCenterPixel = getCenterPixel();
	}

	@Override
	public Region getNode() {
		return this.scrollPane;
	}

	/**
	 * A list of String identifiers which can be used to logically group Nodes,
	 * specifically for an external style engine. This variable is analogous to
	 * the "class" attribute on an HTML element and, as such, each element of
	 * the list is a style class to which this Node belongs.
	 *
	 * @return the style class list.
	 */
	public ObservableList<String> getStyleClass() {
		return this.scrollPane.getStyleClass();
	}

	/**
	 * Defines the mouse cursor for this Node and subnodes. If null, then the
	 * cursor of the first parent node with a non-null cursor will be used. If
	 * no Node in the scene graph defines a cursor, then the cursor of the Scene
	 * will be used.
	 *
	 * @return the cursor property of the zoom pane.
	 */
	public ObjectProperty<Cursor> cursorProperty() {
		return this.scrollPane.cursorProperty();
	}

	/**
	 * Sets the value of the property cursor.
	 *
	 * @param value the cursor value.
	 */
	public void setCursor(Cursor value) {
		this.scrollPane.setCursor(value);
	}

	/**
	 * Gets the value of the property cursor.
	 *
	 * @return the value of the property cursor.
	 */
	public Cursor getCursor() {
		return this.scrollPane.getCursor();
	}

	/**
	 * Sets the content of the zoom pane.
	 *
	 * @param nodes the content of the zoom pane.
	 */
	public void setContent(Node... nodes) {
		this.contentPane.getChildren().setAll(nodes);
		fireContentChange();
	}

	@Override
	public boolean isEmpty() {
		return this.contentPane.getChildren().isEmpty();
	}

	@Override
	public Pane getContentPane() {
		return this.contentPane;
	}

	@Override
	public BooleanProperty contentChangedProperty() {
		return this.contentChangedProperty;
	}

	/**
	 * Fires a content change event. Indicates to observers that the content of
	 * the zoom pane has changed.
	 */
	public void fireContentChange() {
		contentChangedProperty.set(!contentChangedProperty.get());
	}

	/**
	 * The alignment property. Used to distribute extra padding resulting from a
	 * larger viewport than the scaled content. Note that this only applies to
	 * the dynamic extra padding, not the fixed one.
	 *
	 * @return the alignment property.
	 */
	public ObjectProperty<Pos> alignmentProperty() {
		return paddedRegion.alignmentProperty();
	}

	/**
	 * Returns the alignment used to distribute extra padding.
	 *
	 * @return the alginment.
	 */
	public Pos getAlignment() {
		return paddedRegion.getAlignment();
	}

	/**
	 * Sets the alignment used to distribute extra padding.
	 *
	 * @param value the alignment.
	 */
	public void setAlignment(Pos value) {
		paddedRegion.setAlignment(value);
	}

	/**
	 * Expands a region to the bounds of the viewport (if smaller). Updates the
	 * minWidth and minHeight properties of the given region as the viewport
	 * bounds change.
	 *
	 * @param region the region to expand.
	 * @return the attached invalidation listener. Keep this one around in case
	 * you intend to reverse/stop this behaviour (see
	 * {@code removeExpandInViewportListener()}).
	 */
	public InvalidationListener expandInViewport(Region region) {
		final InvalidationListener listener = (c) -> {
			final Bounds bounds = getViewportBounds();
			region.setMinWidth(bounds.getWidth());
			region.setMinHeight(bounds.getHeight());
		};

		this.viewportBoundsProperty().addListener(listener);
		return listener;
	}

	/**
	 * Stops a region from getting expanded to the viewport. This removes the
	 * given invalidation listener, and stops the expanding behaviour.
	 *
	 * @param listener the invalidation listener (returned by
	 * {@code expandInViewport}.
	 */
	public void removeExpandInViewportListener(InvalidationListener listener) {
		this.viewportBoundsProperty().removeListener(listener);
	}

	// zoomable
	@Override
	public DoubleProperty zoomProperty() {
		return this.zoom;
	}

	@Override
	public double getZoomMin() {
		return this.zoomMin;
	}

	@Override
	public double getZoomMax() {
		return this.zoomMax;
	}

	@Override
	public ReadOnlyObjectProperty<Bounds> contentBoundsProperty() {
		return this.contentPane.boundsInLocalProperty();
	}

	@Override
	public ReadOnlyObjectProperty<Bounds> scaledContentBoundsProperty() {
		return this.scalingPane.boundsInParentProperty();
	}

	@Override
	public ReadOnlyObjectProperty<Bounds> paddedContentBoundsProperty() {
		return this.paddedRegion.layoutBoundsProperty();
	}

	// pannable
	@Override
	public ReadOnlyDoubleProperty topPaddingProperty() {
		return this.paddedRegion.topPaddingRealProperty();
	}

	@Override
	public ReadOnlyDoubleProperty rightPaddingProperty() {
		return this.paddedRegion.rightPaddingRealProperty();
	}

	@Override
	public ReadOnlyDoubleProperty bottomPaddingProperty() {
		return this.paddedRegion.bottomPaddingRealProperty();
	}

	@Override
	public ReadOnlyDoubleProperty leftPaddingProperty() {
		return this.paddedRegion.leftPaddingRealProperty();
	}

	@Override
	public ReadOnlyBooleanProperty needsLayoutProperty() {
		return this.scrollPane.needsLayoutProperty();
	}

	@Override
	public ObjectProperty<Bounds> viewportBoundsProperty() {
		return this.scrollPane.viewportBoundsProperty();
	}

	@Override
	public void setViewportPosition(double hpos, double vpos) {
		changeIsLocal = true;
		setHvalue(hpos);
		setVvalue(vpos);
		changeIsLocal = false;
		onPanning();
	}

	@Override
	public void moveToPixel(double x, double y) {
		final Bounds viewportBounds = getViewportBounds();
		final Bounds paddedBounds = getPaddedContentBounds();
		final double scrollableWidth = paddedBounds.getWidth() - viewportBounds.getWidth();
		final double scrollableHeight = paddedBounds.getHeight() - viewportBounds.getHeight();
		changeIsLocal = true;
		if (scrollableWidth <= 0) {
			setHvalue(0);
		} else {
			final double sx = getLeftPadding() + x * getZoom();
			final double hval = (sx - viewportBounds.getWidth() / 2) / scrollableWidth * getHrange();
			setHvalue(clamp(hval, getHmin(), getHmax()));
		}
		if (scrollableHeight <= 0) {
			setVvalue(0);
		} else {
			final double sy = getTopPadding() + y * getZoom();
			final double vval = (sy - viewportBounds.getHeight() / 2) / scrollableHeight * getVrange();
			setVvalue(clamp(vval, getVmin(), getVmax()));
		}
		changeIsLocal = false;
		onPanning();
	}

	@Override
	public Point2D getMoveToPixelPosition(double x, double y) {
		final Bounds viewportBounds = getViewportBounds();
		final Bounds paddedBounds = getPaddedContentBounds();
		final double scrollableWidth = paddedBounds.getWidth() - viewportBounds.getWidth();
		final double scrollableHeight = paddedBounds.getHeight() - viewportBounds.getHeight();

		final double hpos;
		final double vpos;

		if (scrollableWidth <= 0) {
			hpos = 0;
		} else {
			final double sx = getLeftPadding() + x * getZoom();
			final double hval = (sx - viewportBounds.getWidth() / 2) / scrollableWidth * getHrange();
			hpos = clamp(hval, getHmin(), getHmax());
		}
		if (scrollableHeight <= 0) {
			vpos = 0;
		} else {
			final double sy = getTopPadding() + y * getZoom();
			final double vval = (sy - viewportBounds.getHeight() / 2) / scrollableHeight * getVrange();
			vpos = clamp(vval, getVmin(), getVmax());
		}

		return new Point2D(hpos, vpos);
	}

	protected double clamp(double value, double min, double max) {
		if (value <= min) {
			return min;
		} else if (value >= max) {
			return max;
		} else {
			return value;
		}
	}

	@Override
	public Point2D getCenterPixel() {
		final Bounds viewportBounds = getViewportBounds();
		final Bounds paddedBounds = getPaddedContentBounds();
		final double scrollableWidth = paddedBounds.getWidth() - viewportBounds.getWidth();
		final double scrollableHeight = paddedBounds.getHeight() - viewportBounds.getHeight();
		final double scrolledH = getHvalue() / getHrange() * scrollableWidth;
		final double scrolledV = getVvalue() / getVrange() * scrollableHeight;
		final double invZoom = 1 / getZoom();

		return new Point2D(
				((scrolledH + viewportBounds.getWidth() / 2) - getLeftPadding()) * invZoom,
				((scrolledV + viewportBounds.getHeight() / 2) - getTopPadding()) * invZoom
		);
	}

	@Override
	public DoubleProperty hvalueProperty() {
		return this.scrollPane.hvalueProperty();
	}

	@Override
	public final ReadOnlyDoubleProperty hminProperty() {
		return this.scrollPane.hminProperty();
	}

	@Override
	public ReadOnlyDoubleProperty hminRealProperty() {
		return this.hminRealProperty;
	}

	protected void setHminReal(double value) {
		hminRealProperty.set(value);
	}

	@Override
	public final ReadOnlyDoubleProperty hmaxProperty() {
		return this.scrollPane.hmaxProperty();
	}

	@Override
	public ReadOnlyDoubleProperty hmaxRealProperty() {
		return this.hmaxRealProperty;
	}

	protected void setHmaxReal(double value) {
		hmaxRealProperty.set(value);
	}

	@Override
	public DoubleProperty vvalueProperty() {
		return this.scrollPane.vvalueProperty();
	}

	@Override
	public final ReadOnlyDoubleProperty vminProperty() {
		return this.scrollPane.vminProperty();
	}

	@Override
	public ReadOnlyDoubleProperty vminRealProperty() {
		return this.vminRealProperty;
	}

	protected void setVminReal(double value) {
		vminRealProperty.set(value);
	}

	@Override
	public final ReadOnlyDoubleProperty vmaxProperty() {
		return this.scrollPane.vmaxProperty();
	}

	@Override
	public ReadOnlyDoubleProperty vmaxRealProperty() {
		return this.vmaxRealProperty;
	}

	protected void setVmaxReal(double value) {
		vmaxRealProperty.set(value);
	}

	//
	protected double scrollRangeX() {
		return getHmax() - getHmin();
	}

	protected double scrollX() {
		return getHvalue() * scrollRangeX();
	}

	protected double scrollRangeY() {
		return getVmax() - getVmin();
	}

	protected double scrollY() {
		return getVvalue() * scrollRangeY();
	}

}
