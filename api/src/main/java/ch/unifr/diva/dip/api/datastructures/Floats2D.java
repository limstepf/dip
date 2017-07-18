package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.utils.ArrayUtils;
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
public class Floats2D {

	@XmlElement(name = "data")
	public final float[][] data;

	@SuppressWarnings("unused")
	public Floats2D() {
		this(new float[][]{});
	}

	/**
	 * Creates a new, empty 2D array of {@code float}s.
	 *
	 * @param m the number of rows.
	 * @param n the number of columns.
	 */
	public Floats2D(int m, int n) {
		this(new float[m][n]);
	}

	/**
	 * Creates a new 1D array of {@code float}s.
	 *
	 * @param data the data.
	 */
	public Floats2D(float[][] data) {
		this.data = data;
	}

	/**
	 * Returns the number of rows in the 2D array.
	 *
	 * @return the number of rows.
	 */
	public int getNumRows() {
		return data.length;
	}

	/**
	 * Returns the number of columns in the 2D array. This methods assumes that
	 * all rows have the same size. The number of columns of the first row is
	 * returned.
	 *
	 * @return the number of columns.
	 */
	public int getNumColumns() {
		if (data.length == 0) {
			return 0;
		}
		return data[0].length;
	}

	/**
	 * Returns a row of the 2D array.
	 *
	 * @param m the index of the row (or Y coordinate).
	 * @return a row of the 2D array.
	 */
	public float[] getRow(int m) {
		return data[m];
	}

	/**
	 * Returns the value of an element in the 2D array.
	 *
	 * @param m the index of the row (or Y coordinate).
	 * @param n the index of the column (or X coordinate).
	 * @return the value at the given position.
	 */
	public float getValue(int m, int n) {
		return data[m][n];
	}

	/**
	 * Flattens the 2D array to a 1D array. This methods assumes that all rows
	 * have the same size. The number of columns of the first row is used to
	 * determine the number of columns. Missing values from smaller rows will be
	 * zero, values from larger rows will be ignored.
	 *
	 * @return a flattened 1D array of the size
	 * {@code getNumRows() * getNumColumns()}.
	 */
	public float[] flatten() {
		return ArrayUtils.flatten(data);
	}

	/**
	 * Returns a copy.
	 *
	 * @return a copy.
	 */
	public Floats2D copy() {
		return new Floats2D(copyData());
	}

	/**
	 * Returns a copy of the 2D array.
	 *
	 * @return a copy of the 2D array.
	 */
	public float[][] copyData() {
		final float[][] copy = new float[data.length][];
		for (int i = 0; i < data.length; i++) {
			copy[i] = data[i].clone();
		}
		return copy;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{"
				+ "rows=" + getNumRows()
				+ ", columns=" + getNumColumns()
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
		final Floats2D other = (Floats2D) obj;
		if (!Arrays.deepEquals(this.data, other.data)) {
			return false;
		}
		return true;
	}

}
