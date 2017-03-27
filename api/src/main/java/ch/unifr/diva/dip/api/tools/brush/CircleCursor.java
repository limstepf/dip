package ch.unifr.diva.dip.api.tools.brush;

import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * A circle (brush) cursor.
 */
public class CircleCursor extends CrosshairCursor implements BrushCursor {

	protected final Circle circle;

	/**
	 * Creates a new circle (brush) cursor.
	 */
	public CircleCursor() {
		super();

		this.circle = ShapeUtils.newCircleOutline();
		ShapeUtils.setStroke(Color.WHITE, circle);
	}

	@Override
	public void setZoom(double zoom, int strokeWidth) {
		final double inv = 1.0 / zoom;
		final double radius = strokeWidth * .5;
		super.setZoom(zoom, inv);

		circle.setStrokeWidth(inv);

		if (strokeWidth <= 1) {
			getChildren().setAll(hline, vline);
		} else if (strokeWidth <= crosshairThreshold) {
			circle.setRadius(radius);
			getChildren().setAll(circle);
		} else {
			circle.setRadius(radius);
			getChildren().setAll(circle, hline, vline);
		}
	}

}
