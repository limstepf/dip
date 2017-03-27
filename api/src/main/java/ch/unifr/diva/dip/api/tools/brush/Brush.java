package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.scene.Node;

/**
 * A brush. A brush has just a custom cursor (consisting of JavaFX
 * nodes/shapes), and is supposed to be extended with special paint methods
 * (e.g. to get a paint brush).
 */
public interface Brush {

	/**
	 * Returns the name of the brush.
	 *
	 * @return the name of the brush.
	 */
	public String getName();

	/**
	 * Returns the glyph of the brush.
	 *
	 * @return the glyph of the brush.
	 */
	public NamedGlyph getGlyph();

	/**
	 * Returns the stroke width (size, or diameter) of the brush.
	 *
	 * @return the stroke width (size, or diameter) of the brush.
	 */
	public int getStrokeWidth();

	/**
	 * Returns the stroke radius of the brush. This is half the
	 * {@code getStrokeWidth()}.
	 *
	 * @return the stroke radius of the brush.
	 */
	public double getStrokeRadius();

	/**
	 * Sets the zoom factor, and the stroke width of the brush. The zoom factor
	 * (of the zoom pane/overlay) is used to achieve non-scaling strokes, or
	 * constant sized shapes regardless of the zoom level (the overlay layer of
	 * a zoom pane is scaled exactly like the content pane).
	 *
	 * @param zoom the zoom factor.
	 * @param strokeWidth the stroke width of the brush.
	 */
	public void setZoom(double zoom, int strokeWidth);

	/**
	 * Returns the custom cursor of the brush.
	 *
	 * @return the custom cursor of the brush.
	 */
	public Node getCursor();

}
