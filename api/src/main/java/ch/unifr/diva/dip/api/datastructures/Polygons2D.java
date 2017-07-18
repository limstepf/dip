package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D polygons.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Polygons2D extends AbstractList<Polygon2D> implements Cloneable {

	/**
	 * Creates a new, empty list.
	 */
	public Polygons2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D polygons.
	 *
	 * @param elements the list items.
	 */
	public Polygons2D(List<Polygon2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the polygons.
	 *
	 * @return a copy of the polygons.
	 */
	public Polygons2D copy() {
		final Polygons2D copy = new Polygons2D();
		for (Polygon2D poly : elements) {
			copy.add(poly.copy());
		}
		return copy;
	}

}
