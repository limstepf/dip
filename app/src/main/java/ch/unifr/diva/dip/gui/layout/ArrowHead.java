package ch.unifr.diva.dip.gui.layout;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * An arrowhead (or a triangle drawn on a canvas). Can be used together with
 * {@code HLine} to create an arrow by adding the arrowhead(s) to the
 * {@code HLine} as children and binding them to the HLine's width- and
 * y-properties.
 */
public class ArrowHead extends Canvas {

	public enum Direction {

		LEFT,
		RIGHT
	}

	private Direction direction;
	private double height;
	private double width;
	private Color fillColor = Color.BLACK;
	private Color strokeColor = Color.BLACK;
	private double lineWidth = 1.0;

	public ArrowHead() {
		this(Direction.RIGHT);
	}

	public ArrowHead(Direction direction) {
		this(direction, 4);
	}

	public ArrowHead(Direction direction, double height) {
		super(computeWidth(height), computeHeight(height) * 2.0);
		this.direction = direction;
		this.height = computeHeight(height);
		this.width = computeWidth(height);
		this.setManaged(false);
		draw();
	}

	private static double computeHeight(double h) {
		return ((int) h) + .5;
	}

	private static double computeWidth(double h) {
		return ((int) h * 1.95) + .5;
	}

	public void setLineWidth(double width) {
		this.lineWidth = width;
	}

	public void setColor(Color color) {
		setFillColor(color);
		setStrokeColor(color);
	}

	public void setFillColor(Color color) {
		this.fillColor = color;
		draw();
	}

	public void setStrokeColor(Color color) {
		this.strokeColor = color;
		draw();
	}

	private void draw() {
		final GraphicsContext gc = this.getGraphicsContext2D();
		gc.clearRect(0, 0, this.getWidth(), this.getHeight());
		gc.setFill(fillColor);
		gc.setStroke(strokeColor);
		gc.setLineWidth(lineWidth);

		final double[] xPts;
		final double[] yPts;
		final double x0 = 0.5;

		switch (direction) {
			case LEFT:
				xPts = new double[]{
					x0, width, width
				};
				yPts = new double[]{
					height, x0, 2 * height
				};
				break;
			case RIGHT:
			default:
				xPts = new double[]{
					x0, width, x0
				};
				yPts = new double[]{
					x0, height, height * 2
				};
				break;
		}

		gc.fillPolygon(xPts, yPts, 3);
		gc.strokePolygon(xPts, yPts, 3);
	}

	public void bind(ReadOnlyDoubleProperty widthProperty, ReadOnlyDoubleProperty yProperty) {
		if (direction.equals(Direction.RIGHT)) {
			this.layoutXProperty().bind(
					Bindings.subtract(widthProperty, this.widthProperty())
			);
		}
		this.layoutYProperty().bind(
				Bindings.subtract(
						yProperty,
						Bindings.divide(this.heightProperty(), 2.0)
				)
		);
	}

	public void unbind() {
		this.layoutXProperty().unbind();
		this.layoutYProperty().unbind();
	}
}
