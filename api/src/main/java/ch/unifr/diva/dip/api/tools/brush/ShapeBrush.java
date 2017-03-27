package ch.unifr.diva.dip.api.tools.brush;

import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;

/**
 * A shape brush. Returns drawn JavaFX shapes.
 *
 * @param <T> class of the drawn/selected shapes.
 */
public interface ShapeBrush<T extends Shape> extends Brush {

	/**
	 *
	 * @param e the mouse event where {@code e.getX()} and {@code e.getY()} are
	 * the center of the single brush stamp.
	 * @return
	 */
	default T paint(MouseEvent e) {
		return paint(e.getX(), e.getY());
	}

	/**
	 * Paints (or creates the shape of) a single brush stamp.
	 *
	 * @param x the x-coordinate of the center of the single brush stamp.
	 * @param y the y-coordinate of the center of the single brush stamp.
	 * @return the painted shape.
	 */
	public T paint(double x, double y);

	/**
	 * Paints (or creates the shape of) a single brush stamp.
	 *
	 * @param x the x-coordinate of the top-left point of the single brush
	 * stamp.
	 * @param y the y-coordinate of the top-left point of the single brush
	 * stamp.
	 * @return the painted shape.
	 */
	public T paint(int x, int y);

	/**
	 * Paints multiple brush stamps to paint (or creates the shape of) a stroke
	 * between two points.
	 *
	 * @param startX the x-coordinate of the start of the stroke.
	 * @param startY the y-coordinate of the start of the stroke.
	 * @param endX the x-coordinate of the end of the stroke.
	 * @param endY the y-coordinate of the end of the stroke.
	 * @param divisor the divisor. Determines the spacing of single brush stamps
	 * along the stroke (relative to the stroke width). E.g. a divisor of 4
	 * paints a new brush stamp after 25% of the stroke width.
	 * @return the painted shape.
	 */
	default Shape paintStroke(double startX, double startY, double endX, double endY, int divisor) {
		final double dx = endX - startX;
		final double dy = endY - startY;
		final double dist = Math.sqrt(dx * dx + dy * dy);
		final double steps = (dist / ((double) getStrokeWidth() / (double) divisor)) + 1;
		final double incX = dx / steps;
		final double incY = dy / steps;

		final int n = (int) (steps + .5);
		double x = startX - getStrokeRadius();
		double y = startY - getStrokeRadius();
		Shape shape = null;

		for (int i = 0; i < n; i++) {
			x += incX;
			y += incY;
			final Shape s = paint(
					(int) Math.round(x),
					(int) Math.round(y)
			);
			shape = (shape == null) ? s : Shape.union(shape, s);
		}

		return shape;
	}

}
