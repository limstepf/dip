package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * A square (brush) cursor.
 */
public class SquareCursor extends CrosshairCursor implements BrushCursor {

	protected final Rectangle rect;

	/**
	 * Creates a new square (brush) cursor.
	 */
	public SquareCursor() {
		super();

		this.rect = ShapeUtils.newRectangleOutline();
		ShapeUtils.setStroke(Color.WHITE, rect);
	}

	@Override
	public void setZoom(double zoom, int strokeWidth) {
		final double inv = 1.0 / zoom;
		final double radius = (strokeWidth * .5);

		super.setZoom(zoom, inv);
		rect.setStrokeWidth(inv);

		final double zrad = radius * zoom;
		final double r = (zrad - Math.floor(zrad)) * inv;

		if (strokeWidth <= 1) {
			getChildren().setAll(hline, vline);
		} else if (strokeWidth <= crosshairThreshold) {
			final double swr = strokeWidth - 2*r;
			rect.setWidth(strokeWidth);
			rect.setHeight(strokeWidth);
			rect.setX(r - radius);
			rect.setY(r - radius);
			getChildren().setAll(rect);
		} else {
			final double swr = strokeWidth - 2*r;
			rect.setWidth(swr);
			rect.setHeight(swr);
			rect.setX(r - radius);
			rect.setY(r - radius);
			getChildren().setAll(rect, hline, vline);
		}
	}

}
