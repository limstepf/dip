package ch.unifr.diva.dip.api.datastructures;

import java.awt.Rectangle;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 2D rectangle in integer-precision. Used to un/-marshall
 * {@code java.awt.Rectangle}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class IntegerRectangle {

	@XmlAttribute
	public final int x;

	@XmlAttribute
	public final int y;

	@XmlAttribute
	public final int width;

	@XmlAttribute
	public final int height;

	@SuppressWarnings("unused")
	public IntegerRectangle() {
		this(0, 0, 0, 0);
	}

	/**
	 * Creates a new 2D rectangle.
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param width the width.
	 * @param height the height.
	 */
	public IntegerRectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Creates a new 2D rectangle.
	 *
	 * @param rect the AWT rectangle.
	 */
	public IntegerRectangle(Rectangle rect) {
		this.x = rect.x;
		this.y = rect.y;
		this.width = rect.width;
		this.height = rect.height;
	}

	/**
	 * Returns the rectangle as {@code java.awt.Rectangle}.
	 *
	 * @return the rectangle as {@code java.awt.Rectangle}.
	 */
	public Rectangle toAWTRectangle() {
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Returns the rectangle as {@code javafx.geometry.Rectangle2D}.
	 *
	 * @return the rectangle as {@code javafx.geometry.Rectangle2D}.
	 */
	public javafx.geometry.Rectangle2D toFXRectangle2D() {
		return new javafx.geometry.Rectangle2D(x, y, width, height);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ "x=" + x
				+ ", y=" + y
				+ ", width=" + width
				+ ", height=" + height
				+ "}";
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + Integer.hashCode(x);
		hash = 31 * hash + Integer.hashCode(y);
		hash = 31 * hash + Integer.hashCode(width);
		hash = 31 * hash + Integer.hashCode(height);
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
		final IntegerRectangle other = (IntegerRectangle) obj;
		if (!Objects.equals(x, other.x)) {
			return false;
		}
		if (!Objects.equals(y, other.y)) {
			return false;
		}
		if (!Objects.equals(width, other.width)) {
			return false;
		}
		return Objects.equals(height, other.height);
	}

}
