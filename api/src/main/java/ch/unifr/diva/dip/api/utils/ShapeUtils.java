package ch.unifr.diva.dip.api.utils;

import java.awt.Point;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;

/**
 * JavaFX shapes utilities.
 */
public class ShapeUtils {

	private ShapeUtils() {
		// nope
	}

	/**
	 * Calculates the distance between two points.
	 *
	 * @param x0 the x-coordinate of the first point.
	 * @param y0 the y-coordinate of the first point.
	 * @param x1 the x-coordinate of the second point.
	 * @param y1 the y-coordinate of the second point.
	 * @return the distance between the two points.
	 */
	public static double distance(double x0, double y0, double x1, double y1) {
		final double dx = x1 - x0;
		final double dy = y1 - y0;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Spans a rectangle between two points.
	 *
	 * @param rect the rectangle.
	 * @param x0 the X-coordinate of the first point.
	 * @param y0 the Y-coordinate of the first point.
	 * @param x1 the X-coordinate of the second point.
	 * @param y1 the Y-coordinate of the second point.
	 */
	public static void spanRectangle(Rectangle rect, double x0, double y0, double x1, double y1) {
		rect.setX(Math.floor(Math.min(x0, x1)));
		rect.setY(Math.floor(Math.min(y0, y1)));
		rect.setWidth(Math.ceil(Math.abs(x1 - x0)));
		rect.setHeight(Math.ceil(Math.abs(y1 - y0)));
	}

	/**
	 * Spans an ellipse between two points.
	 *
	 * @param ellipse the ellipse.
	 * @param x0 the X-coordinate of the first point.
	 * @param y0 the Y-coordinate of the first point.
	 * @param x1 the X-coordinate of the second point.
	 * @param y1 the Y-coordinate of the second point.
	 */
	public static void spanEllipse(Ellipse ellipse, double x0, double y0, double x1, double y1) {
		final int startx = (int) x0;
		final int endx = (int) Math.round(x1);
		final int dx = endx - startx;

		final int starty = (int) y0;
		final int endy = (int) Math.round(y1);
		final int dy = endy - starty;

		ellipse.setCenterX(startx + dx * .5);
		ellipse.setCenterY(starty + dy * .5);
		ellipse.setRadiusX(Math.abs(dx) * .5);
		ellipse.setRadiusY(Math.abs(dy) * .5);
	}

	/**
	 * Creates a new rectangle outline with a black stroke.
	 *
	 * @return a new rectangle outline.
	 */
	public static Rectangle newRectangleOutline() {
		return outline(new Rectangle());
	}

	public static Rectangle newRectangleExclusionOutline() {
		return exclusionOutline(new Rectangle());
	}

	/**
	 * Creates a new circle outline with a black stroke.
	 *
	 * @return a new circle outline.
	 */
	public static Circle newCircleOutline() {
		return outline(new Circle());
	}

	public static Circle newCircleExclusionOutline() {
		return exclusionOutline(new Circle());
	}

	/**
	 * Creates a new ellipse outline with a black stroke.
	 *
	 * @return a new ellipse outline.
	 */
	public static Ellipse newEllipseOutline() {
		return outline(new Ellipse());
	}

	/**
	 * Creates a new ellipse exclusion outline.
	 *
	 * @return a new ellipse exclusion outline.
	 */
	public static Ellipse newEllipseExclusionOutline() {
		return exclusionOutline(new Ellipse());
	}

	/**
	 * Creates a new exclusion polyine.
	 *
	 * @return a new exclusion polyine.
	 */
	public static Polyline newExclusionPolyline() {
		return newExclusionPolyline(StrokeType.INSIDE, 1.0);
	}

	/**
	 * Creates a new exclusion polyine.
	 *
	 * @param strokeType the stroke type.
	 * @param strokeWidth the stroke width.
	 * @return a new exclusion polyine.
	 */
	public static Polyline newExclusionPolyline(StrokeType strokeType, double strokeWidth) {
		final Polyline shape = new Polyline();
		shape.setStroke(Color.WHITE);
		shape.setStrokeWidth(strokeWidth);
		shape.setBlendMode(BlendMode.EXCLUSION);
		return shape;
	}

	/**
	 * Outlines a shape. Sets the fill to transparent, and the stroke to black.
	 *
	 * @param <T> class of the shape.
	 * @param shape the shape.
	 * @return the outlined shape.
	 */
	public static <T extends Shape> T outline(T shape) {
		return outline(shape, Color.BLACK, StrokeType.INSIDE, 1.0);
	}

	/**
	 * Outlines a shape. Sets the fill to transparent, and the stroke to the
	 * given specifications.
	 *
	 * @param <T> class of the shape.
	 * @param shape the shape.
	 * @param color the stroke color.
	 * @param strokeType the stroke type.
	 * @param strokeWidth the stroke width.
	 * @return the outlined shape.
	 */
	public static <T extends Shape> T outline(T shape, Color color, StrokeType strokeType, double strokeWidth) {
		shape.setFill(Color.TRANSPARENT);
		shape.setStroke(color);
		shape.setStrokeType(strokeType);
		shape.setStrokeWidth(strokeWidth);
		return shape;
	}

	/**
	 * Outlines a shape with exclusion blend mode.
	 *
	 * @param <T> class of the shape.
	 * @param shape the shape.
	 * @return the outlined shape.
	 */
	public static <T extends Shape> T exclusionOutline(T shape) {
		return exclusionOutline(shape, StrokeType.INSIDE, 1.0);
	}

	/**
	 * Outlines a shape with exclusion blend mode.
	 *
	 * @param <T> class of the shape.
	 * @param shape the shape.
	 * @param strokeType the stroke type.
	 * @param strokeWidth the stroke width.
	 * @return the outlined shape.
	 */
	public static <T extends Shape> T exclusionOutline(T shape, StrokeType strokeType, double strokeWidth) {
		shape.setFill(Color.TRANSPARENT);
		shape.setStroke(Color.WHITE);
		shape.setStrokeType(strokeType);
		shape.setStrokeWidth(strokeWidth);
		shape.setBlendMode(BlendMode.EXCLUSION);
		return shape;
	}

	/**
	 * Prepares a shape to be used as mask. This method removes the stroke and
	 * sets a default fill.
	 *
	 * @param <T> class of the shape.
	 * @param shape the shape.
	 * @return the shape prepared to be used as a mask.
	 */
	public static <T extends Shape> T prepareMask(T shape) {
		shape.setFill(Color.BLACK);
		shape.setStroke(null);
		return shape;
	}

	/**
	 * Sets the fill of the given shape(s).
	 *
	 * @param color the fill color.
	 * @param shapes the shape(s).
	 */
	public static void setFill(Color color, Shape... shapes) {
		for (Shape shape : shapes) {
			shape.setFill(color);
		}
	}

	/**
	 * Sets the stroke of the given shape(s).
	 *
	 * @param color the stroke color.
	 * @param shapes the shape(s).
	 */
	public static void setStroke(Color color, Shape... shapes) {
		for (Shape shape : shapes) {
			shape.setStroke(color);
		}
	}

	/**
	 * Sets the blend mode of the given shape(s).
	 *
	 * @param mode the blend mode.
	 * @param shapes the shape(s).
	 */
	public static void setBlendMode(BlendMode mode, Shape... shapes) {
		for (Shape shape : shapes) {
			shape.setBlendMode(mode);
		}
	}

	/**
	 * Draws a shape to the graphics context.
	 *
	 * @param gc the graphics context.
	 * @param shape the shape.
	 */
	public static void drawShapeToGraphicsContext(GraphicsContext gc, Shape shape) {
		if (shape instanceof Rectangle) {
			drawRectangleToGraphicsContext(gc, (Rectangle) shape);
		} else if (shape instanceof Ellipse) {
			drawEllipseToGraphicsContext(gc, (Ellipse) shape);
		} else if (shape instanceof Path) {
			drawPathToGraphicsContext(gc, (Path) shape);
		} else if (shape instanceof Arc) {
			drawArcToGraphicsContext(gc, (Arc) shape);
		} else {
			throw new UnsupportedOperationException("don't know how to draw: " + shape);
		}
	}

	/**
	 * Draws a rectangle to the graphics context.
	 *
	 * @param gc the graphics context.
	 * @param shape the rectangle.
	 */
	public static void drawRectangleToGraphicsContext(GraphicsContext gc, Rectangle shape) {
		gc.beginPath();
		gc.rect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
		gc.closePath();
	}

	/**
	 * Draws an ellipse to the graphics context.
	 *
	 * @param gc the graphics context.
	 * @param shape the ellipse.
	 */
	public static void drawEllipseToGraphicsContext(GraphicsContext gc, Ellipse shape) {
		gc.beginPath();
		gc.arc(
				shape.getCenterX(), shape.getCenterY(),
				shape.getRadiusX(), shape.getRadiusY(),
				0,
				360
		);
		gc.closePath();
	}

	/**
	 * Draws a path to the graphics context.
	 *
	 * @param gc the graphics context.
	 * @param shape the path.
	 */
	public static void drawPathToGraphicsContext(GraphicsContext gc, Path shape) {
		gc.beginPath();
		for (PathElement pe : shape.getElements()) {
			if (pe instanceof MoveTo) {
				final MoveTo move = (MoveTo) pe;
				gc.moveTo(move.getX(), move.getY());
			} else if (pe instanceof LineTo) {
				final LineTo line = (LineTo) pe;
				gc.lineTo(line.getX(), line.getY());
			} else if (pe instanceof QuadCurveTo) {
				final QuadCurveTo quad = (QuadCurveTo) pe;
				gc.quadraticCurveTo(
						quad.getControlX(), quad.getControlY(),
						quad.getX(), quad.getY()
				);
			} else if (pe instanceof CubicCurveTo) {
				final CubicCurveTo cubic = (CubicCurveTo) pe;
				gc.bezierCurveTo(
						cubic.getControlX1(), cubic.getControlY1(),
						cubic.getControlX2(), cubic.getControlY2(),
						cubic.getX(), cubic.getY()
				);
			} else if (pe instanceof ClosePath) {
				gc.closePath();
			} else {
				throw new UnsupportedOperationException("don't know how to draw: " + pe + " from " + shape);
			}
		}
	}

	/**
	 * Draws an arc to the graphics context.
	 *
	 * @param gc the graphics context.
	 * @param shape the arc.
	 */
	public static void drawArcToGraphicsContext(GraphicsContext gc, Arc shape) {
		gc.beginPath();
		gc.arc(
				shape.getCenterX(), shape.getCenterY(),
				shape.getRadiusX(), shape.getRadiusY(),
				shape.getStartAngle(), shape.getLength()
		);
		gc.closePath();
	}

	/**
	 * A shape pixel iterator. Iterates over all pixel covered by the given
	 * shape (e.g. a selection mask). The area covered is approximated
	 * (overestimated) by the bounding box of the intersection of the shape with
	 * a given pixel. Whether or not a pixel is considered inside the shape can
	 * be controlled with an (area) threshold from 0 (no coverage at all) to 1.0
	 * (full pixel coverage).
	 */
	public static class PixelIterator implements Iterable<Point>, Iterator<Point> {

		private final Shape shape;
		private final double threshold;
		private final int minX;
		private final int minY;
		private final int maxX;
		private final int maxY;
		private int currentX;
		private int currentY;
		private Point next;

		/**
		 * Creates a new shape pixel iterator with default threshold. The
		 * default threshold is pretty much one (minus some epsilon).
		 *
		 * @param shape the shape.
		 */
		public PixelIterator(Shape shape) {
			this(shape, 0.995);
		}

		/**
		 * Creates a new shape pixel iterator.
		 *
		 * @param shape the shape.
		 * @param threshold the area coverage threshold (in the interval: ]0,
		 * 1]).
		 */
		public PixelIterator(Shape shape, double threshold) {
			this.shape = shape;
			this.threshold = threshold;
			final Bounds b = shape.getBoundsInLocal();
			this.minX = (int) b.getMinX();
			this.minY = (int) b.getMinY();
			this.maxX = (int) Math.ceil(b.getMaxX());
			this.maxY = (int) Math.ceil(b.getMaxY());

			currentX = minX;
			currentY = minY;

			seek();
		}

		private void seek() {
			for (int y = currentY; y < maxY; y++) {
				for (int x = currentX; x < maxX; x++) {
					final Shape pixel = new Rectangle(x, y, 1, 1);
					final Shape intersection = Shape.intersect(shape, pixel);
					final Bounds b = intersection.getBoundsInLocal();
					if (computeArea(b) >= threshold) {
						next = new Point(x, y);
						if ((x + 1) < maxX) {
							currentX++;
						} else {
							currentY++;
							currentX = minX;
						}
						return;
					}
				}
			}
			next = null;
		}

		private double computeArea(Bounds b) {
			final double minx = b.getMinX();
			final double maxx = b.getMaxX();
			final double dx = maxx - minx;
			if (dx <= 0) {
				return 0;
			}
			final double miny = b.getMinY();
			final double maxy = b.getMaxY();
			final double dy = maxy - miny;
			if (dy <= 0) {
				return 0;
			}
			return dx * dy;
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Point next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			final Point ret = next;
			seek();
			return ret;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<Point> iterator() {
			return this;
		}

	}

}
