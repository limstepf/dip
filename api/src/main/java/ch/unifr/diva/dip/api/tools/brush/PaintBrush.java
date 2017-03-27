package ch.unifr.diva.dip.api.tools.brush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 * A paint brush. Paints on a JavaFX {@code GraphicsContext}.
 */
public interface PaintBrush extends Brush {

	/**
	 * Paints a single brush stamp.
	 *
	 * @param gc the graphics context.
	 * @param e the mouse event where {@code e.getX()} and {@code e.getY()} are
	 * the center of the single brush stamp.
	 */
	default void paint(GraphicsContext gc, MouseEvent e) {
		paint(gc, e.getX(), e.getY());
	}

	/**
	 * Paints a single brush stamp.
	 *
	 * @param gc the graphics context.
	 * @param x the x-coordinate of the center of the single brush stamp.
	 * @param y the y-coordinate of the center of the single brush stamp.
	 */
	public void paint(GraphicsContext gc, double x, double y);

	/**
	 * Paints a single brush stamp.
	 *
	 * @param gc the graphics context.
	 * @param x the x-coordinate of the top-left point of the single brush
	 * stamp.
	 * @param y the y-coordinate of the top-left point of the single brush
	 * stamp.
	 */
	public void paint(GraphicsContext gc, int x, int y);

	/**
	 * Paints multiple brush stamps to paint a stroke between two points.
	 *
	 * @param gc the graphics context.
	 * @param startX the x-coordinate of the start of the stroke.
	 * @param startY the y-coordinate of the start of the stroke.
	 * @param endX the x-coordinate of the end of the stroke.
	 * @param endY the y-coordinate of the end of the stroke.
	 * @param divisor the divisor. Determines the spacing of single brush stamps
	 * along the stroke (relative to the stroke width). E.g. a divisor of 4
	 * paints a new brush stamp after 25% of the stroke width.
	 */
	default void paintStroke(GraphicsContext gc, double startX, double startY, double endX, double endY, int divisor) {
		final double dx = endX - startX;
		final double dy = endY - startY;
		final double dist = Math.sqrt(dx * dx + dy * dy);
		final double steps = (dist / ((double) getStrokeWidth() / (double) divisor)) + 1;
		final double incX = dx / steps;
		final double incY = dy / steps;

		final int n = (int) (steps + .5);
		double x = startX - getStrokeRadius();
		double y = startY - getStrokeRadius();

		for (int i = 0; i < n; i++) {
			x += incX;
			y += incY;
			paint(
					gc,
					(int) Math.round(x),
					(int) Math.round(y)
			);
		}
	}
}
