package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.scene.Node;

/**
 * A circle brush.
 */
public class CircleBrush extends BrushBase {

	protected final CircleCursor cursor;

	/**
	 * Creates a new circle brush.
	 *
	 * @param name the name of the brush.
	 * @param glyph the glyph of the brush.
	 */
	public CircleBrush(String name, NamedGlyph glyph) {
		super(name, glyph);
		this.cursor = new CircleCursor();
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
