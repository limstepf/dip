package ch.unifr.diva.dip.api.datastructures;

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
}
