package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A list of {@code NestedShape2D}.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NestedShapes2D extends AbstractList<NestedShape2D> {

	/**
	 * Creates a new, empty list.
	 */
	public NestedShapes2D() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a new list of {@code NestedShape2D}.
	 *
	 * @param elements the list items.
	 */
	public NestedShapes2D(List<NestedShape2D> elements) {
		super(elements);
	}

	/**
	 * Returns a copy of the {@code NestedShapes2D}.
	 *
	 * @return a copy of the {@code NestedShapes2D}.
	 */
	public NestedShapes2D copy() {
		final NestedShapes2D copy = new NestedShapes2D();
		for (NestedShape2D point : elements) {
			copy.add(point); // no need to copy; points are final
		}
		return copy;
	}

}
