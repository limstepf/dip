
package ch.unifr.diva.dip.api.datastructures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D line defined by two {@code Point2D}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Line2D {

	@XmlElement
	public final Point2D end;
	@XmlElement
	public final Point2D start;

	@SuppressWarnings("unused")
	public Line2D() {
		this(null, null);
	}

	public Line2D(double startX, double startY, double endX, double endY) {
		this(new Point2D(startX, startY), new Point2D(endX, endY));
	}

	public Line2D(Point2D start, Point2D end) {
		this.end = end;
		this.start = start;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "start=" + start
				+ ", end=" + end
				+ "}";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
