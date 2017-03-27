package ch.unifr.diva.dip.api.tools.brush;

/**
 * A brush cursor. The hotspot (or center) of these cursors is at (0,0).
 *
 * <p>
 * Brush cursors typically adjust their size (at least parts of the
 * cursor/shapes if mixed with a regular/constant size cursor) with the zoom
 * factor. To get sharp edges (and have them sit on the middle of a pixel)
 * positions and dimensions should be adjusted by the scaled back "snapping
 * difference": {@code (pos - Math.floor(pos)) * (1.0 / zoom)} (for
 * areas/shapes), or {@code (.5 - (pos - Math.floor(pos))) * (1.0 / zoom)} (for
 * edges/lines).
 */
public interface BrushCursor extends Cursor {

	/**
	 * Sets the zoom factor, and the stroke width of the brush. The zoom factor
	 * (of the zoom pane/overlay) is used to achieve non-scaling strokes, or
	 * constant sized shapes regardless of the zoom level (the overlay layer of
	 * a zoom pane is scaled exactly like the content pane).
	 *
	 * <p>
	 * For brush cursors this method needs to be called instead of the
	 * {@code setZoom(double zoom)} of a regular cursor, in order to set zoom
	 * and stroke width at the same time.
	 *
	 * @param zoom the zoom factor.
	 * @param strokeWidth the stroke width of the brush.
	 */
	public void setZoom(double zoom, int strokeWidth);

}
