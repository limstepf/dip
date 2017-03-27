package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * A square shape brush.
 */
public class SquareShapeBrush extends SquareBrush implements ShapeBrush {

	/**
	 * Creates a new square shape brush.
	 *
	 * @param name the name of the brush.
	 * @param glyph the glyph of the brush.
	 */
	public SquareShapeBrush(String name, NamedGlyph glyph) {
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
				new Rectangle(x, y, getStrokeWidth(), getStrokeWidth())
		);
	}

}
