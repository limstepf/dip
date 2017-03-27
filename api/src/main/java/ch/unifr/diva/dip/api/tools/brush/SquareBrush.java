package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.scene.Node;

/**
 * A square brush.
 */
public class SquareBrush extends BrushBase {

	protected final SquareCursor cursor;

	/**
	 * Creates a new square brush.
	 *
	 * @param name the name of the brush.
	 * @param glyph the glyph of the brush.
	 */
	public SquareBrush(String name, NamedGlyph glyph) {
		super(name, glyph);
		this.cursor = new SquareCursor();
	}

	@Override
	public void setZoom(double zoom, int strokeWidth) {
		putStrokeWidth(strokeWidth);
		cursor.setZoom(zoom, strokeWidth);
	}

	@Override
	public Node getCursor() {
		return cursor;
	}

}
