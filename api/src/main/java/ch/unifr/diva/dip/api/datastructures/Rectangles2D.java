package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D rectangles.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Rectangles2D extends AbstractList<Rectangle2D> {

	/**
	 * Creates a new, empty list.
	 */
	public Rectangles2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D circles.
	 *
	 * @param elements the list items.
	 */
	public Rectangles2D(List<Rectangle2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the circles.
	 *
	 * @return a copy of the circles.
	 */
	public Rectangles2D copy() {
		final Rectangles2D copy = new Rectangles2D();
		for (Rectangle2D poly : elements) {
			copy.add(poly.copy());
		}
		return copy;
	}

}
