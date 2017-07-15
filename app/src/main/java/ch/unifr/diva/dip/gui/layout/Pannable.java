package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.api.utils.FxUtils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;

/**
 * Interface of a pannable component. A pannable component is also zoomable, and
 * may have a padded region around the scaled content.
 */
public interface Pannable extends Zoomable {

	/**
	 * Padding method used for panning.
	 */
	public enum Padding {

		/**
		 * No padding.
		 */
		NONE,
		/**
		 * Viewport padding. Adds a dynamic padding to the pannable region
		 * biased by the dimensions of the viewport. This is usually used s.t. a
		 * certain, small area of the scaled content pane can not be scrolled
		 * completely outside the viewport.
		 */
		VIEWPORT;

		/**
		 * Safely returns the padding method by its name.
		 *
		 * @param name name of the padding method.
		 * @return the requested padding method, or the default one if invalid.
		 */
		public static Padding get(String name) {
			try {
				return Padding.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return getDefault();
			}
		}

		/**
		 * Returns the default padding method.
		 *
		 * @return the default padding method.
		 */
		public static Padding getDefault() {
			return VIEWPORT;
		}

	}

	/**
	 * Returns the padded content bounds property. These are the scaled and
	 * padded content bounds. The padding might be zero, so these bounds are
	 * guaranteed to be equal or greater than the scaled content bounds.
	 *
	 * @return the padded content bounds property.
	 */
	public ReadOnlyObjectProperty<Bounds> paddedContentBoundsProperty();

	/**
	 * Returns the padded content bounds. These are the scaled and padded
	 * content bounds. The padding might be zero, so these bounds are guaranteed
	 * to be equal or greater than the scaled content bounds.
	 *
	 * @return the padded content bounds.
	 */
	default Bounds getPaddedContentBounds() {
		return paddedContentBoundsProperty().get();
	}

	/**
	 * The top padding property.
	 *
	 * @return the top padding property.
	 */
	public ReadOnlyDoubleProperty topPaddingProperty();

	/**
	 * Returns the top padding.
	 *
	 * @return the top padding.
	 */
	default double getTopPadding() {
		return topPaddingProperty().get();
	}

	/**
	 * The right padding property.
	 *
	 * @return the right padding property.
	 */
	public ReadOnlyDoubleProperty rightPaddingProperty();

	/**
	 * Returns the right padding.
	 *
	 * @return the right padding.
	 */
	default double getRightPadding() {
		return rightPaddingProperty().get();
	}

	/**
	 * The bottom padding property.
	 *
	 * @return the bottom padding property.
	 */
	public ReadOnlyDoubleProperty bottomPaddingProperty();

	/**
	 * Returns the bottom padding.
	 *
	 * @return the bottom padding.
	 */
	default double getBottomPadding() {
		return bottomPaddingProperty().get();
	}

	/**
	 * The left padding property.
	 *
	 * @return the left padding property.
	 */
	public ReadOnlyDoubleProperty leftPaddingProperty();

	/**
	 * Returns the left padding.
	 *
	 * @return the left padding.
	 */
	default double getLeftPadding() {
		return leftPaddingProperty().get();
	}

	/**
	 * The needs layout property of the pannable component.
	 *
	 * @return the needs layout property of the pannable component.
	 */
	public ReadOnlyBooleanProperty needsLayoutProperty();

	/**
	 * The viewport bounds property of the pannable component. These are usually
	 * the viewport bounds of the backing scrollpane.
	 *
	 * @return the viewport bounds property of the pannable component.
	 */
	public ObjectProperty<Bounds> viewportBoundsProperty();

	/**
	 * Returns the viewport bounds of the pannable component. These are usually
	 * the viewport bounds of the backing scrollpane.
	 *
	 * @return the viewport bounds of the pannable component.
	 */
	default Bounds getViewportBounds() {
		return viewportBoundsProperty().get();
	}

	/**
	 * Sets the viewport position. This method updates the {@code Hvalue} and
	 * {@code Vvalue} (of the backing scrollpane) in an atomic way, s.t.
	 * internal observers update their thing only once, not twice.
	 *
	 * @param hpos the horizontal scroll position/value.
	 * @param vpos the vertical scroll position/value.
	 */
	public void setViewportPosition(double hpos, double vpos);

	/**
	 * Scrolls/moves the pannable content to the given pixel. This method tries
	 * to center the given pixel (using the coordinate system of the unscaled
	 * content pane) in the viewport (capped by minimal/maximal scroll
	 * positions/values).
	 *
	 * @param x the x-coordinate in the unscaled content pane.
	 * @param y the y-coordinate in the unscaled content pane.
	 */
	public void moveToPixel(double x, double y);

	/**
	 * Scrolls/moves the pannable content to the given pixel. This method tries
	 * to center the given pixel (using the coordinate system of the unscaled
	 * content pane) in the viewport (capped by minimal/maximal scroll
	 * positions/values).
	 *
	 * @param pixel the pixel coordinates in the unscaled content pane.
	 */
	default void moveToPixel(Point2D pixel) {
		moveToPixel(pixel.getX(), pixel.getY());
	}

	/**
	 * Scrolls/moves the pannable content to a relative position.
	 *
	 * @param value the relative position.
	 */
	default void moveToPos(Pos value) {
		moveToPos(value, 0);
	}

	/**
	 * Scrolls/moves the pannable content to a relative position.
	 *
	 * @param value the relative position.
	 * @param offset an offset to shift the viewport outwards into the padded
	 * region (if present/possible). E.g. for a {@code TOP_LEFT} position, this
	 * offset moves the viewport back on the x- and y-axis, s.t. the padded
	 * region is visible to the top and to the left.
	 */
	default void moveToPos(Pos value, double offset) {
		final double invZoom = 1 / getZoom();
		final Bounds contentBounds = getContentBounds();
		final Bounds viewportBounds = getViewportBounds();
		final double viewportWidth = viewportBounds.getWidth() * invZoom;
		final double viewportHeight = viewportBounds.getHeight() * invZoom;

		final double x;
		final double y;

		switch (value.getHpos()) {
			default:
			case LEFT:
				x = viewportWidth / 2 - offset;
				break;
			case CENTER:
				x = contentBounds.getWidth() / 2;
				break;
			case RIGHT:
				x = contentBounds.getWidth() - viewportWidth / 2 + offset;
				break;
		}

		switch (value.getVpos()) {
			default:
			case TOP:
				y = viewportHeight / 2 - offset;
				break;
			case BASELINE:
			case CENTER:
				y = contentBounds.getHeight() / 2;
				break;
			case BOTTOM:
				y = contentBounds.getHeight() - viewportHeight / 2 + offset;
				break;
		}

		moveToPixel(x, y);
	}

	/**
	 * Return the horizontal and vertical scroll position to the given pixel.
	 * This method tries to center the given pixel (using the coordinate system
	 * of the unscaled content pane) in the viewport (capped by minimal/maximal
	 * scroll positions/values).
	 *
	 * @param x the x-coordinate in the unscaled content pane.
	 * @param y the y-coordinate in the unscaled content pane.
	 * @return the horizontal (x as hpos) and vertical (y as vpos) scroll
	 * positions.
	 */
	public Point2D getMoveToPixelPosition(double x, double y);

	/**
	 * Return the horizontal and vertical scroll position to the given pixel.
	 * This method tries to center the given pixel (using the coordinate system
	 * of the unscaled content pane) in the viewport (capped by minimal/maximal
	 * scroll positions/values).
	 *
	 * @param pixel the pixel coordinates in the unscaled content pane.
	 * @return the horizontal (x as hpos) and vertical (y as vpos) scroll
	 * positions.
	 */
	default Point2D getMoveToPixelPosition(Point2D pixel) {
		return getMoveToPixelPosition(pixel.getX(), pixel.getY());
	}

	/**
	 * Returns the currently centered pixel in the viewport.
	 *
	 * @return the centered pixel coordinates in the viewport, w.r.t. the
	 * unscaled content pane.
	 */
	public Point2D getCenterPixel();

	/**
	 * The hvalue property. This is the current horizontal scroll position of
	 * the pannable.
	 *
	 * @return the hvalue property.
	 */
	public DoubleProperty hvalueProperty();

	/**
	 * Sets the current horizontal scroll position of the pannable.
	 *
	 * @param pos the horizontal scroll position.
	 */
	default void setHvalue(double pos) {
		hvalueProperty().set(pos);
	}

	/**
	 * Returns the current horizontal scroll position of the pannable.
	 *
	 * @return the current horizontal scroll position of the pannable.
	 */
	default double getHvalue() {
		return hvalueProperty().get();
	}

	/**
	 * The hmin property. This is the minimum allowable horizontal scroll
	 * position.
	 *
	 * @return the hmin property.
	 */
	public ReadOnlyDoubleProperty hminProperty();

	/**
	 * Returns the minimum allowable horizontal scroll position.
	 *
	 * @return the minimum allowable horizontal scroll position.
	 */
	default double getHmin() {
		return hminProperty().get();
	}

	/**
	 * The hmin real property. This is similar to the hmin property, but points
	 * to the position where the actual content starts due to padding. Hence the
	 * hmin real value is equal to (with no padding) or larger than the hmin
	 * value.
	 *
	 * @return the hmin real property.
	 */
	public ReadOnlyDoubleProperty hminRealProperty();

	/**
	 * Returns the real minimum horizontal scroll position.
	 *
	 * @return the real minimum horizontal scroll position.
	 */
	default double getHminReal() {
		return hminRealProperty().get();
	}

	/**
	 * The hmax property. This is the maximum allowable horizontal scroll
	 * position.
	 *
	 * @return the hmax property.
	 */
	public ReadOnlyDoubleProperty hmaxProperty();

	/**
	 * Returns the maximum allowable horizontal scroll position.
	 *
	 * @return the maximum allowable horizontal scroll position.
	 */
	default double getHmax() {
		return hmaxProperty().get();
	}

	/**
	 * The hmax real property. This is similar to the hmax property, but points
	 * to the position where the actual content ends due to padding. Hence the
	 * hmax real value is smaller than or equal to (with no padding) to the hmax
	 * value.
	 *
	 * @return the hmax real property.
	 */
	public ReadOnlyDoubleProperty hmaxRealProperty();

	/**
	 * Returns the real maximum horizontal scroll position.
	 *
	 * @return the real maximum horizontal scroll position.
	 */
	default double getHmaxReal() {
		return hmaxRealProperty().get();
	}

	/**
	 * Return the horizontal scrolling range. This equals
	 * {@code getHmax() - getHmin()}.
	 *
	 * @return the horizontal scrolling range.
	 */
	default double getHrange() {
		return getHmax() - getHmin();
	}

	/**
	 * Returns the real horizontal scrolling range. This equals
	 * {@code getHmaxReal() - getHminReal()}, and as such is equal to or smaller
	 * than the horizontal scrolling range returned by {@code getHrange()}.
	 *
	 * @return the real horizontal scrolling range.
	 */
	default double getHrangeReal() {
		return getHmaxReal() - getHminReal();
	}

	/**
	 * The vvalue property. This is the current vertical scroll position of the
	 * pannable.
	 *
	 * @return the vvalue property.
	 */
	public DoubleProperty vvalueProperty();

	/**
	 * Sets the current vertical scroll position of the pannable.
	 *
	 * @param pos the vertical scroll position.
	 */
	default void setVvalue(double pos) {
		vvalueProperty().set(pos);
	}

	/**
	 * Returns the current vertical scroll position of the pannable.
	 *
	 * @return the current vertical scroll position of the pannable.
	 */
	default double getVvalue() {
		return vvalueProperty().get();
	}

	/**
	 * The vmin property. This is the minimum allowable vertical scroll
	 * position.
	 *
	 * @return the vmin property.
	 */
	public ReadOnlyDoubleProperty vminProperty();

	/**
	 * Returns the minimum allowable vertical scroll position.
	 *
	 * @return the minimum allowable vertical scroll position.
	 */
	default double getVmin() {
		return vminProperty().get();
	}

	/**
	 * The vmin real property. This is similar to the vmin property, but points
	 * to the position where the actual content starts due to padding. Hence the
	 * vmin real value is equal to (with no padding) or larger than the vmin
	 * value.
	 *
	 * @return the vmin real property.
	 */
	public ReadOnlyDoubleProperty vminRealProperty();

	/**
	 * Returns the real minimum vertical scroll position.
	 *
	 * @return the real minimum vertical scroll position.
	 */
	default double getVminReal() {
		return vminRealProperty().get();
	}

	/**
	 * The vmax property. This is the maximum allowable vertical scroll
	 * position.
	 *
	 * @return the vmax property.
	 */
	public ReadOnlyDoubleProperty vmaxProperty();

	/**
	 * Returns the maximum allowable vertical scroll position.
	 *
	 * @return the maximum allowable vertical scroll position.
	 */
	default double getVmax() {
		return vmaxProperty().get();
	}

	/**
	 * The vmax real property. This is similar to the vmax property, but points
	 * to the position where the actual content ends due to padding. Hence the
	 * vmax real value is smaller than or equal (with no padding) to the vmax
	 * value.
	 *
	 * @return the vmax real property.
	 */
	public ReadOnlyDoubleProperty vmaxRealProperty();

	/**
	 * Returns the real maximum vertical scroll position.
	 *
	 * @return the real maximum vertical scroll position.
	 */
	default double getVmaxReal() {
		return vmaxRealProperty().get();
	}

	/**
	 * Return the vertical scrolling range. This equals
	 * {@code getVmax() - getVmin()}.
	 *
	 * @return the vertical scrolling range.
	 */
	default double getVrange() {
		return getVmax() - getVmin();
	}

	/**
	 * Returns the real vertical scrolling range. This equals
	 * {@code getVmaxReal() - getVminReal()}, and as such is equal to or smaller
	 * than the vertical scrolling range returned by {@code getVrange()}.
	 *
	 * @return the real vertical scrolling range.
	 */
	default double getVrangeReal() {
		return getVmaxReal() - getVminReal();
	}

	/**
	 * Calculates the visible region in the viewport of the pannable.
	 *
	 * @return the visible region.
	 */
	default VisibleRegion getVisibleRegion() {
		return new VisibleRegion(this);
	}

	/**
	 * The visible region in the viewport of the pannable.
	 */
	public static class VisibleRegion {

		/**
		 * The zoom factor.
		 */
		public final double zoom;
		/**
		 * The viewport bounds.
		 */
		public final Bounds viewportBounds;
		/**
		 * The scaled content bounds.
		 */
		public final Bounds scaledBounds;
		/**
		 * The padded (and scaled) content bounds.
		 */
		public final Bounds paddedBounds;
		/**
		 * The scrollable width.
		 */
		public final double scrollableWidth;
		/**
		 * The scrollable height.
		 */
		public final double scrollableHeight;
		/**
		 * The horizontally scrolled area to the left, outside the viewport.
		 */
		public final double scrolledH;
		/**
		 * The vertically scrolled area at the top, outside the viewport.
		 */
		public final double scrolledV;
		/**
		 * The left padding.
		 */
		public final double leftPadding;
		/**
		 * The top padding.
		 */
		public final double topPadding;
		/**
		 * The intersection of the viewport with the scaled content bounds.
		 */
		public final Rectangle2D intersection;

		/**
		 * Calculates the visible region in the viewport of the pannable. This
		 * is done w.r.t. the scaled content pane. The visible region w.r.t the
		 * original, unscaled content pane can be retrieved in a second step by
		 * a call to {@code getUnscaledVisibleRegion()}.
		 *
		 * @param pannable the pannable component.
		 */
		public VisibleRegion(Pannable pannable) {
			zoom = pannable.getZoom();
			viewportBounds = pannable.getViewportBounds();
			scaledBounds = pannable.getScaledContentBounds();
			paddedBounds = pannable.getPaddedContentBounds();
			scrollableWidth = paddedBounds.getWidth() - viewportBounds.getWidth();
			scrollableHeight = paddedBounds.getHeight() - viewportBounds.getHeight();
			scrolledH = pannable.getHvalue() / pannable.getHrange() * scrollableWidth;
			scrolledV = pannable.getVvalue() / pannable.getVrange() * scrollableHeight;
			leftPadding = pannable.getLeftPadding();
			topPadding = pannable.getTopPadding();

			/*
			 * apply left-/topPadding to the viewportRect instead of the contentRect
			 * (with reversed signs) s.t. we get the proper intersection in the
			 * scalingPane. Carefull there with the snapping to pixel/rounding...
			 */
			final Rectangle2D viewportRect = new Rectangle2D(
					scrolledH - leftPadding,
					scrolledV - topPadding,
					viewportBounds.getWidth(),
					viewportBounds.getHeight()
			);
			final Rectangle2D contentRect = new Rectangle2D(
					0,
					0,
					Math.floor(scaledBounds.getWidth()),
					Math.floor(scaledBounds.getHeight())
			);

			intersection = FxUtils.intersection(viewportRect, contentRect);
		}

		/**
		 * Checks whether there is no visible region.
		 *
		 * @return {@code true} if nothing of the content pane is displayed,
		 * that is, outside of the bounds of the viewport, {@code false} if
		 * something is visible.
		 */
		public boolean isEmpty() {
			return FxUtils.isEmpty(intersection);
		}

		/**
		 * Returns the scaled, visible width.
		 *
		 * @return the scaled, visible width.
		 */
		public double getScaledWidth() {
			return intersection.getWidth();
		}

		/**
		 * Returns the scaled, visible height.
		 *
		 * @return the scaled, visible height.
		 */
		public double getScaledHeight() {
			return intersection.getHeight();
		}

		/**
		 * Returns the x-offset of the visible region {@literal w.r.t} the
		 * scaled content pane.
		 *
		 * @return the x-offset of the visible region in the scaled region.
		 */
		public double getScaledOffsetX() {
			return intersection.getMinX();
		}

		/**
		 * Returns the y-offset of the visible region {@literal w.r.t} the
		 * scaled content pane.
		 *
		 * @return the y-offset of the visible region in the scaled region.
		 */
		public double getScaledOffsetY() {
			return intersection.getMinY();
		}

		/**
		 * Returns the x-offset of the visible region {@literal w.r.t} the
		 * padded (and scaled) content pane.
		 *
		 * @return the x-offset of the visible region in the padded region.
		 */
		public double getPaddedOffsetX() {
			return leftPadding + intersection.getMinX();
		}

		/**
		 * Returns the y-offset of the visible region {@literal w.r.t} the
		 * padded (and scaled) content pane.
		 *
		 * @return the y-offset of the visible region in the padded region.
		 */
		public double getPaddedOffsetY() {
			return topPadding + intersection.getMinY();
		}

		/**
		 * Returns the scaled, visible region.
		 *
		 * @return the scaled, visible region.
		 */
		public Rectangle2D getScaledVisibleRegion() {
			return intersection;
		}

		/**
		 * Returns the unscaled, visible region.
		 *
		 * @return the unscaled, visible region.
		 */
		public Rectangle2D getUnscaledVisibleRegion() {
			final double invZoom = 1.0 / zoom;
			return new Rectangle2D(
					intersection.getMinX() * invZoom,
					intersection.getMinY() * invZoom,
					intersection.getWidth() * invZoom,
					intersection.getHeight() * invZoom
			);
		}

		/**
		 * Returns the unscaled, visible region in subpixel precision.
		 *
		 * @return the unscaled, visible region in subpixel precision.
		 */
		public SubpixelRectangle2D getUnscaledVisibleSubpixelRegion() {
			return new SubpixelRectangle2D(intersection, zoom);
		}

	}

	/**
	 * A subpixel rectangle data object used for upscaling with subpixel
	 * precision.
	 *
	 * <pre>
	 *                1 pixel
	 *                |----|
	 *                .    .
	 *      a'---+----+----+----b'      a,b,c,d in double precision
	 *      |  a |. . |. . | b  |
	 *      +----+----+----+----+       a' = floor(a_x), floor(a_y)
	 *      |  . |    |    | .  |       b' = ceil(a_x),  floor(a_y)
	 *      +----+----+----+----+       c' = floor(a_x), ceil(a_y)
	 *      |  . |    |    | .  |       d' = ceil(a_x),  ceil(a_y)
	 *      +----+----+----+----+
	 *      |  c | . .| . .| d  |       -> compute differences, and scale up
	 *      c'---+----+----+----d'         to get shiftX/Y, restX/Y
	 * </pre>
	 */
	public static class SubpixelRectangle2D {

		/**
		 * The number of shifted repeated pixels on the x-axis.
		 */
		public final int shiftX;
		/**
		 * The rest of the repeated pixels on the x-axis.
		 */
		public final int restX;
		/**
		 * The number of the shifted repeated pixels on the y-axis.
		 */
		public final int shiftY;
		/**
		 * The rest of the repeated pixels on the y-axis.
		 */
		public final int restY;
		/**
		 * The smallest X coordinate of the unscaled, visible region in integer
		 * precision.
		 */
		public final int minX;
		/**
		 * The smallest Y coordinate of the unscaled, visible region in integer
		 * precision.
		 */
		public final int minY;
		/**
		 * The width of the unscaled, visible region in integer precision.
		 */
		public final int width;
		/**
		 * The height of the unscaled, visible region in integer precision.
		 */
		public final int height;

		/**
		 * Creates a new subpixel rectangle data object.
		 *
		 * @param region the (intersection) region in double precision.
		 * @param zoom the zoom.
		 */
		public SubpixelRectangle2D(Rectangle2D region, double zoom) {
			final double invZoom = 1.0 / zoom;
			final double minxReal = region.getMinX() * invZoom;
			final double minyReal = region.getMinY() * invZoom;
			final double widthReal = region.getWidth() * invZoom;
			final double heightReal = region.getHeight() * invZoom;
			this.minX = (int) Math.floor(minxReal);
			this.minY = (int) Math.floor(minyReal);
			this.width = (int) Math.ceil(minxReal + widthReal) - minX;
			this.height = (int) Math.ceil(minyReal + heightReal) - minY;
			final double sx = minxReal - minX;
			final double sy = minyReal - minY;
			this.shiftX = (int) (sx * zoom);
			this.shiftY = (int) (sy * zoom);
			this.restX = (int) ((width - widthReal - sx) * zoom);
			this.restY = (int) ((height - heightReal - sy) * zoom);
		}

		/**
		 * Returns the unscaled, visible region.
		 *
		 * @return the unscaled, visible region.
		 */
		public Rectangle2D getRectangle2D() {
			return new Rectangle2D(minX, minY, width, height);
		}

		/**
		 * Returns the unscaled, visible region.
		 *
		 * @return the unscaled, visible region.
		 */
		public java.awt.Rectangle getRectangle() {
			return new java.awt.Rectangle(minX, minY, width, height);
		}

	}

}
