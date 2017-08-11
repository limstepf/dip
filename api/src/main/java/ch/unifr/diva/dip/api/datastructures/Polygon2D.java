package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D polygon defined by a list of {@code Point2D}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Polygon2D extends Polyline2D {

	@SuppressWarnings("unused")
	public Polygon2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new 2D polygon.
	 *
	 * @param points the points defining the 2D polygon.
	 */
	public Polygon2D(List<Point2D> points) {
		super(points);
	}

	/**
	 * Returns a copy of the polygon.
	 *
	 * @return a copy of the polygon.
	 */
	@Override
	public Polygon2D copy() {
		final Polygon2D copy = new Polygon2D();
		for (Point2D point : elements) {
			copy.add(point); // no need to copy; points are final
		}
		return copy;
	}

	@Override
	public Polygon2D toPolygon2D() {
		return copy();
	}

}
