package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A nested 2D shape. Nested shapes have a parent shape, and child shapes.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class NestedShape2D extends AbstractNestedShape2D<Shape2D, NestedShape2D> {

	@SuppressWarnings("unused")
	public NestedShape2D() {
		this(null, new ArrayList<>());
	}

	/**
	 * Creates a new, nested 2D shape without children.
	 *
	 * @param shape the shape.
	 */
	public NestedShape2D(Shape2D shape) {
		this(shape, new ArrayList<>());
	}

	/**
	 * Creates a new, nested 2D shape.
	 *
	 * @param shape the shape.
	 * @param children the child shapes.
	 */
	public NestedShape2D(Shape2D shape, List<NestedShape2D> children) {
		super(shape, children);
	}

	@Override
	public NestedShape2D copy() {
		return new NestedShape2D(
				shape,
				new ArrayList<>(children)
		);
	}
}
