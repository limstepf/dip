
package ch.unifr.diva.dip.api.datastructures;

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
		this(null);
	}

	public Polygon2D(List<Point2D> points) {
		super(points);
	}

}
