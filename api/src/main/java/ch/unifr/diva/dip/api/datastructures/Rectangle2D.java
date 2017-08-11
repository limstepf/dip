package ch.unifr.diva.dip.api.datastructures;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D rectangle in double-precision floating points. Used to un/-marshall
 * {@code javafx.geometry.Rectangle2D}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Rectangle2D implements Shape2D {

	@XmlAttribute
	public final double x;

	@XmlAttribute
	public final double y;

	@XmlAttribute
	public final double width;

	@XmlAttribute
	public final double height;

	@SuppressWarnings("unused")
	public Rectangle2D() {
		this(0, 0, 0, 0);
	}

	/**
	 * Creates a new 2D rectangle.
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param width the width.
	 * @param height the height.
	 */
	public Rectangle2D(double x, double y, double width, double height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new 2D rectangle.
	 *
	 * @param rect the JavaFX rectangle.
	 */
	public Rectangle2D(javafx.geometry.Rectangle2D rect) {
		this.x = rect.getMinX();
		this.y = rect.getMinY();
		this.width = rect.getWidth();
		this.height = rect.getHeight();
	}

	/**
	 * Returns the rectangle as {@code javafx.geometry.Rectangle2D}.
	 *
	 * @return the rectangle as {@code javafx.geometry.Rectangle2D}.
	 */
	public javafx.geometry.Rectangle2D toFXRectangle2D() {
		return new javafx.geometry.Rectangle2D(x, y, width, height);
	}

	@Override
	public Rectangle2D copy() {
		return this; // no need to copy, since all fields are final
	}

	@Override
	public Polygon2D toPolygon2D() {
		final Polygon2D polygon = new Polygon2D();
		final double x2 = x + width;
		final double y2 = y + height;
		polygon.add(new Point2D(x, y));
		polygon.add(new Point2D(x2, y));
		polygon.add(new Point2D(x2, y2));
		polygon.add(new Point2D(x, y2));
		return polygon;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ "x=" + x
				+ ", y=" + y
				+ ", width=" + width
				+ ", height=" + height
				+ "}";
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + Double.hashCode(x);
		hash = 31 * hash + Double.hashCode(y);
		hash = 31 * hash + Double.hashCode(width);
		hash = 31 * hash + Double.hashCode(height);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Rectangle2D other = (Rectangle2D) obj;
		if (!Objects.equals(x, other.x)) {
			return false;
		}
		if (!Objects.equals(y, other.y)) {
			return false;
		}
		if (!Objects.equals(width, other.width)) {
			return false;
		}
		return Objects.equals(height, other.height);
	}

}
