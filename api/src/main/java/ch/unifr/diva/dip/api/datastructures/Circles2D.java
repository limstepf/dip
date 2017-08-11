package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D circles.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Circles2D extends AbstractList<Circle2D> {

	/**
	 * Creates a new, empty list.
	 */
	public Circles2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D circles.
	 *
	 * @param elements the list items.
	 */
	public Circles2D(List<Circle2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the circles.
	 *
	 * @return a copy of the circles.
	 */
	public Circles2D copy() {
		final Circles2D copy = new Circles2D();
		for (Circle2D poly : elements) {
			copy.add(poly.copy());
		}
		return copy;
	}

}
