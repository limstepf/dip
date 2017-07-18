package ch.unifr.diva.dip.api.datastructures;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A 1D array of {@code float}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Floats1D {

	@XmlElement(name = "data")
	public final float[] data;

	@SuppressWarnings("unused")
	public Floats1D() {
		this(new float[]{});
	}

	/**
	 * Creates a new, empty 1D array of {@code float}s.
	 *
	 * @param n the size of the array.
	 */
	public Floats1D(int n) {
		this(new float[n]);
	}

	/**
	 * Creates a new 1D array of {@code float}s.
	 *
	 * @param data the data.
	 */
	public Floats1D(float[] data) {
		this.data = data;
	}

	/**
	 * Returns the size (or length) of the 1D array.
	 *
	 * @return the size (or length) of the 1D array.
	 */
	public int size() {
		return data.length;
	}

	/**
	 * Returns the value of an element in the 1D array.
	 *
	 * @param idx index of the element.
	 * @return value at the given position.
	 */
	public float getValue(int idx) {
		return data[idx];
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy.
	 */
	public Floats1D copy() {
		return new Floats1D(copyData());
	}

	/**
	 * Returns a copy of the 1D array.
	 *
	 * @return a copy of the 1D array.
	 */
	public float[] copyData() {
		return data.clone();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ Arrays.toString(data)
				+ "}";
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + Arrays.hashCode(data);
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
		final Floats1D other = (Floats1D) obj;
		if (!Arrays.equals(this.data, other.data)) {
			return false;
		}
		return true;
	}

}
