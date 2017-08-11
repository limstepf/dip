package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A nested 2D rectangle. Nested shapes have a parent shape, and child shapes.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NestedRectangle2D extends AbstractNestedShape2D<Rectangle2D, NestedRectangle2D> {

	@SuppressWarnings("unused")
	public NestedRectangle2D() {
		this(0, 0, 0, 0, new ArrayList<>());
	}

	/**
	 * Creates a new, nested 2D rectangle without children.
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param width the width.
	 * @param height the height.
	 */
	public NestedRectangle2D(double x, double y, double width, double height) {
		this(x, y, width, height, new ArrayList<>());
	}

	/**
	 * Creates a new, nested 2D rectangle.
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param width the width.
	 * @param height the height.
	 * @param children the child rectangles.
	 */
	public NestedRectangle2D(double x, double y, double width, double height, List<NestedRectangle2D> children) {
		this(new Rectangle2D(x, y, width, height), children);
	}

	/**
	 * Creates a new, nested 2D rectangle without children.
	 *
	 * @param shape the rectangle.
	 */
	public NestedRectangle2D(Rectangle2D shape) {
		this(shape, new ArrayList<>());
	}

	/**
	 * Creates a new, nested 2D rectangle.
	 *
	 * @param shape the rectangle.
	 * @param children the child rectangles.
	 */
	public NestedRectangle2D(Rectangle2D shape, List<NestedRectangle2D> children) {
		super(shape, children);
	}

	/**
	 * Returns the X-coordinate of the rectangle.
	 *
	 * @return the X-coordinate of the rectangle.
	 */
	public double getX() {
		return shape.x;
	}

	/**
	 * Returns the Y-coordinate of the rectangle.
	 *
	 * @return the Y-coordinate of the rectangle.
	 */
	public double getY() {
		return shape.y;
	}

	/**
	 * Returns the width of the rectangle.
	 *
	 * @return the width of the rectangle.
	 */
	public double getWidth() {
		return shape.width;
	}

	/**
	 * Returns the height of the rectangle.
	 *
	 * @return the height of the rectangle.
	 */
	public double getHeight() {
		return shape.height;
	}

	@Override
	public NestedRectangle2D copy() {
		return new NestedRectangle2D(
				shape,
				new ArrayList<>(children)
		);
	}

}
