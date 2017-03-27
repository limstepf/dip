package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Shape;

/**
 * A circle shape brush.
 */
public class CircleShapeBrush extends CircleBrush implements ShapeBrush {

	/**
	 * Creates a new circle shape brush.
	 *
	 * @param name the name of the brush.
	 * @param glyph the glyph of the brush.
	 */
	public CircleShapeBrush(String name, NamedGlyph glyph) {
		super(name, glyph);
	}

	@Override
	public Shape paint(double x, double y) {
		return paint(
				(int) Math.round(x - getStrokeRadius()),
				(int) Math.round(y - getStrokeRadius())
		);
	}

	@Override
	public Shape paint(int x, int y) {
		return ShapeUtils.prepareMask(
				new Ellipse(
						x + getStrokeRadius(),
						y + getStrokeRadius(),
						getStrokeRadius(),
						getStrokeRadius()
				)
		);
	}

}
