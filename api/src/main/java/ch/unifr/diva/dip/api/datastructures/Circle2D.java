package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D circle.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Circle2D implements Shape2D {

	@XmlAttribute
	public final double x;

	@XmlAttribute
	public final double y;

	@XmlAttribute
	public final double radius;

	@SuppressWarnings("unused")
	public Circle2D() {
		this(0, 0, 0);
	}

	/**
	 * Creates a new circle.
	 *
	 * @param x the X coordinate of the center.
	 * @param y the Y coordinate of the center.
	 * @param radius the radius.
	 */
	public Circle2D(double x, double y, double radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	/**
	 * Returns the center of the circle.
	 *
	 * @return the center of the circle.
	 */
	public Point2D getCenter() {
		return new Point2D(x, y);
	}

	@Override
	public Circle2D copy() {
		return this; // no need to copy, since all fields are final
	}

	@Override
	public Polygon2D toPolygon2D() {
		final Point2D[] points = new Point2D[360];
		for (int i = 0; i < 90; i++) {
			final double theta = Math.toRadians(i);
			final double dx = radius * Math.cos(theta);
			final double dy = radius * Math.sin(theta);
			points[i] = new Point2D(x + dx, y + dy);
			points[i + 90] = new Point2D(x - dy, y + dx);
			points[i + 180] = new Point2D(x - dx, y - dy);
			points[i + 270] = new Point2D(x + dy, y - dx);
		}
		return new Polygon2D(new ArrayList<>(Arrays.asList(points)));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ "x=" + x
				+ ", y=" + y
				+ ", radius=" + radius
				+ "}";
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + Double.hashCode(x);
		hash = 31 * hash + Double.hashCode(y);
		hash = 31 * hash + Double.hashCode(radius);
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
		final Circle2D other = (Circle2D) obj;
		if (!Objects.equals(x, other.x)) {
			return false;
		}
		if (!Objects.equals(y, other.y)) {
			return false;
		}
		return Objects.equals(radius, other.radius);
	}

}
