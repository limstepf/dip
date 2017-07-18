package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of 2D lines.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Lines2D extends AbstractList<Line2D> {

	/**
	 * Creates a new, empty list.
	 */
	public Lines2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of 2D lines.
	 *
	 * @param elements the list items.
	 */
	public Lines2D(List<Line2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the lines.
	 *
	 * @return a copy of the lines.
	 */
	public Lines2D copy() {
		final Lines2D copy = new Lines2D();
		for (Line2D line : elements) {
			copy.add(line); // no need to copy; lines are final, so are points
		}
		return copy;
	}

}
