package ch.unifr.diva.dip.api.datastructures;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D polyline defined by a list of {@code Point2D}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Polyline2D {

	@XmlElement(name = "point")
	public final List<Point2D> points;

	@SuppressWarnings("unused")
	public Polyline2D() {
		this(null);
	}

	/**
	 * Creates a new 2D polyline.
	 *
	 * @param points the 2D points defining the polyline.
	 */
	public Polyline2D(List<Point2D> points) {
		this.points = points;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ points
				+ "}";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Polyline2D clone = (Polyline2D) super.clone();
		for (int i = 0; i < clone.points.size(); i++) {
			clone.points.set(i, (Point2D) clone.points.get(i).clone());
		}
		return clone;
	}

	@Override
	public int hashCode() {
		return points.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Polyline2D other = (Polyline2D) obj;
		return this.points.equals(other.points);
	}

}
