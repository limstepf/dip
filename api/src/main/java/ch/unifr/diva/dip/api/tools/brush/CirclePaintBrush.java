package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.scene.canvas.GraphicsContext;

/**
 * A circle paint brush.
 */
public class CirclePaintBrush extends CircleBrush implements PaintBrush {

	/**
	 * Creates a new circle paint brush.
	 *
	 * @param name the name of the brush.
	 * @param glyph the glyph of the brush.
	 */
	public CirclePaintBrush(String name, NamedGlyph glyph) {
		super(name, glyph);
	}

	@Override
	public void paint(GraphicsContext gc, double x, double y) {
		paint(
				gc,
				(int) Math.round(x - getStrokeRadius()),
				(int) Math.round(y - getStrokeRadius())
		);
	}

	@Override
	public void paint(GraphicsContext gc, int x, int y) {
		gc.fillOval(x, y, getStrokeWidth(), getStrokeWidth());
	}

}
