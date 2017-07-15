package ch.unifr.diva.dip.api.tools.brush;

/**
 * A custom (brush) cursor. The hotspot (or center) of these cursors is at
 * (0,0).
 *
 * <p>
 * Cursors typically maintain the same size, no matter what zoom factor. To get
 * sharp edges, they must land on the middle of a pixel, which is achieved by
 * offsetting lines by {@code .5 * (1.0 / zoom)}.
 */
public interface Cursor {

	/**
	 * Sets the zoom factor. The zoom factor (of the zoom pane/overlay) is used
	 * to achieve non-scaling strokes, or constant sized shapes regardless of
	 * the zoom level (the overlay layer of a zoom pane is scaled exactly like
	 * the content pane).
	 *
	 * @param zoom the zoom factor.
	 */
	public void setZoom(double zoom);

	/**
	 * Toggles the visibility of the custom cursor. Typically already
	 * implemented by JavaFX {@code Node} (or {@code Group}) from which a cursor
	 * extends from.
	 *
	 * @param value {@code true} to make the cursor visible, {@code false} to
	 * hide it.
	 */
	public void setVisible(boolean value);

	/**
	 * Sets the X-coordinate of the cursor. Typically already implemented by
	 * JavaFX {@code Node} (or {@code Group}) from which a cursor extends from.
	 *
	 * @param value the X-coordinate of the cursor.
	 */
	public void setLayoutX(double value);

	/**
	 * Sets the Y-coordinate of the cursor. Typically already implemented by
	 * JavaFX {@code Node} (or {@code Group}) from which a cursor extends from.
	 *
	 * @param value the Y-coordinate of the cursor.
	 */
	public void setLayoutY(double value);

}
