package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D shapes.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Shapes2D extends AbstractList<Shape2D> {

	/**
	 * Creates a new, empty list.
	 */
	public Shapes2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D shapes.
	 *
	 * @param elements the list items.
	 */
	public Shapes2D(List<Shape2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the shapes.
	 *
	 * @return a copy of the shapes.
	 */
	public Shapes2D copy() {
		final Shapes2D copy = new Shapes2D();
		for (Shape2D shape : elements) {
			copy.add(shape.copy());
		}
		return copy;
	}

}
