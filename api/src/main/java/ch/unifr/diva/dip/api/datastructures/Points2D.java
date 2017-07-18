package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D points.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Points2D extends AbstractList<Point2D> {

	/**
	 * Creates a new, empty list.
	 */
	public Points2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D points.
	 *
	 * @param elements the list items.
	 */
	public Points2D(List<Point2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the points.
	 *
	 * @return a copy of the points.
	 */
	public Points2D copy() {
		final Points2D copy = new Points2D();
		for (Point2D point : elements) {
			copy.add(point); // no need to copy; points are final
		}
		return copy;
	}

}
