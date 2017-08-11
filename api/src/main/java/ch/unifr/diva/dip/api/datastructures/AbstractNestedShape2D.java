package ch.unifr.diva.dip.api.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A nested 2D shape. Nested shapes are shapes with child shapes. The parent
 * relationship is implicit (i.e. not doubly linked).
 *
 * @param <S> the type of the shape.
 * @param <T> the type of the nested shape.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractNestedShape2D<S extends Shape2D, T extends AbstractNestedShape2D<S, T>> implements Shape2D {

	@XmlElement(type = Object.class)
	public final S shape;

	@XmlElement(type = Object.class, name = "children")
	public final List<T> children;

	@SuppressWarnings("unused")
	public AbstractNestedShape2D() {
		this(null, new ArrayList<>());
	}

	/**
	 * Creates a new, nested 2D shape.
	 *
	 * @param shape the shape.
	 * @param children the child shapes.
	 */
	public AbstractNestedShape2D(S shape, List<T> children) {
		this.shape = shape;
		this.children = children;
	}

	/**
	 * Returns the shape.
	 *
	 * @return the shape.
	 */
	public S getShape() {
		return shape;
	}

	/**
	 * Returns the child shapes.
	 *
	 * @return the child shapes.
	 */
	public List<T> getChildren() {
		return children;
	}

	/**
	 * Checks whether this shape has children, or not.
	 *
	 * @return {@code true} if this shape has children, {@code false} otherwise.
	 */
	public boolean hasChildren() {
		if (children == null) {
			return false;
		}
		return !children.isEmpty();
	}

	@Override
	public abstract AbstractNestedShape2D<?, ?> copy();

	@Override
	public Polygon2D toPolygon2D() {
		return shape.toPolygon2D();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ "shape=" + shape
				+ ", children=" + ((children == null) ? 0 : children.size())
				+ "}";
	}

	@Override
	public int hashCode() {
		int hash = shape.hashCode();
		if (children != null) {
			hash = 31 * hash + children.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractNestedShape2D<?, ?> other = (AbstractNestedShape2D<?, ?>) obj;
		if (!Objects.equals(this.shape, other.shape)) {
			return false;
		}
		if (!Objects.equals(this.children, other.children)) {
			return false;
		}
		return true;
	}

}
