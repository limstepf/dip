package ch.unifr.diva.dip.api.datastructures;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D point (backed by two doubles).
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Point2D implements Cloneable {

	@XmlAttribute
	public final double x;
	@XmlAttribute
	public final double y;

	@SuppressWarnings("unused")
	public Point2D() {
		this(0.0, 0.0);
	}

	/**
	 * Creates a new 2D point.
	 *
	 * @param x the X coordinate of the point.
	 * @param y the Y coordinate of the point.
	 */
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "x=" + x
				+ ", y=" + y
				+ "}";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + Double.hashCode(x);
		hash = 31 * hash + Double.hashCode(y);
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
		final Point2D other = (Point2D) obj;
		if (!Objects.equals(x, other.x)) {
			return false;
		}
		return Objects.equals(y, other.y);
	}

}
