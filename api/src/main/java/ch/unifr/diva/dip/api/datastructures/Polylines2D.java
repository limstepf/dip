package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D polylines.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Polylines2D extends AbstractList<Polyline2D> {

	/**
	 * Creates a new, empty list.
	 */
	public Polylines2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D polylines.
	 *
	 * @param elements the list items.
	 */
	public Polylines2D(List<Polyline2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the polylines.
	 *
	 * @return a copy of the polylines.
	 */
	public Polylines2D copy() {
		final Polylines2D copy = new Polylines2D();
		for (Polyline2D poly : elements) {
			copy.add(poly.copy());
		}
		return copy;
	}

}
