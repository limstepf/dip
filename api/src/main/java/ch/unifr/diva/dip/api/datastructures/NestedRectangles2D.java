package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of {@code NestedRectangle2D}.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NestedRectangles2D extends AbstractList<NestedRectangle2D> {

	/**
	 * Creates a new, empty list.
	 */
	public NestedRectangles2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of {@code NestedRectangle2D}.
	 *
	 * @param elements the list items.
	 */
	public NestedRectangles2D(List<NestedRectangle2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of {@code NestedRectangles2D}.
	 *
	 * @return a copy of {@code NestedRectangles2D}.
	 */
	public NestedRectangles2D copy() {
		final NestedRectangles2D copy = new NestedRectangles2D();
		for (NestedRectangle2D point : elements) {
			copy.add(point); // no need to copy; points are final
		}
		return copy;
	}

}
