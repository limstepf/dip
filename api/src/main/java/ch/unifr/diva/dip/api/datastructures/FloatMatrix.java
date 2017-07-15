package ch.unifr.diva.dip.api.datastructures;

import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple, marshallable 2D matrix class backed by a continous/linear array of
 * {@code float}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FloatMatrix extends Matrix<FloatMatrix> {

	/**
	 * Continous floats of the matrix.
	 */
	@XmlElement
	final public float[] data;

	@SuppressWarnings("unused")
	public FloatMatrix() {
		this(0, 0, Layout.ROW_MAJOR_ORDER);
	}

	/**
	 * Creates a new, zeroed m-by-n matrix. Backed by a continuous/linear array
	 * in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 */
	public FloatMatrix(int rows, int columns) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, new float[rows * columns]);
	}

	/**
	 * Creates a new, zeroed m-by-n matrix.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 */
	public FloatMatrix(int rows, int columns, Layout layout) {
		this(rows, columns, layout, new float[rows * columns]);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array. Backed by a
	 * continuous/linear array in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param data continous data array of m times n floats in row-major order.
	 */
	public FloatMatrix(int rows, int columns, float... data) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, data);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 * @param data continous data array of m times n floats.
	 */
	public FloatMatrix(int rows, int columns, Layout layout, float... data) {
		super(rows, columns, layout);

		this.data = data;
	}

	/**
	 * Creates a matrix with the values and shape defined by the given 2D array.
	 * The data is assumed to be in row-major order.
	 *
	 * @param matrix the 2D array representation of the matrix.
	 */
	public FloatMatrix(float[][] matrix) {
		super(matrix.length, matrix[0].length, Layout.ROW_MAJOR_ORDER);

		this.data = new float[this.rows * this.columns];

		int pos = 0;
		for (int i = 0; i < this.rows; i++) {
			final float[] row = matrix[i];

			if (row.length != this.columns) {
				throw new IllegalArgumentException(String.format(
						"Illegal row length in row %d: %d (expected: %d)",
						i,
						row.length,
						this.columns
				));
			}

			System.arraycopy(row, 0, this.data, pos, this.columns);
			pos += this.columns;
		}
	}

	/**
	 * Returns the single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @return the element at the specified row/column.
	 */
	public float get(int row, int column) {
		return get(index(row, column));
	}

	/**
	 * Returns the single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @return the element at the specified index.
	 */
	public float get(int index) {
		return this.data[index];
	}

	/**
	 * Converts the matrix to a 2D array of floats in row-major order.
	 *
	 * @return a 2D array of floats in row-major order.
	 */
	public float[][] getArray2D() {
		final float[][] a = new float[this.rows][this.columns];

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				a[i][j] = get(i, j);
			}
		}

		return a;
	}

	/**
	 * Returns the matrix as a kernel with floating point coefficients.
	 *
	 * @return a kernel with floating point coefficients.
	 */
	public java.awt.image.Kernel getKernel() {
		if (!this.layout.equals(Layout.ROW_MAJOR_ORDER)) {
			return this.toRowMajor().getKernel();
		}

		return new java.awt.image.Kernel(this.columns, this.rows, this.data);
	}

	@Override
	public String get(int row, int column, String format) {
		return String.format(format, get(row, column));
	}

	/**
	 * Sets a single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public FloatMatrix set(int row, int column, float value) {
		return set(index(row, column), value);
	}

	/**
	 * Sets a single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public FloatMatrix set(int index, float value) {
		this.data[index] = value;
		return this;
	}

	/**
	 * Sets all elements in the matrix to the new value.
	 *
	 * @param value the new value of all elements.
	 * @return the modified matrix.
	 */
	public FloatMatrix fill(float value) {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] = value;
		}
		return this;
	}

	/**
	 * Adds a value to all elements in the matrix.
	 *
	 * @param value the value to add to all elements.
	 * @return the modified matrix.
	 */
	public FloatMatrix add(float value) {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] += value;
		}
		return this;
	}

	/**
	 * Multiplies all elements in the matrix with the given value.
	 *
	 * @param value the value to multiply all elements with.
	 * @return the modified matrix.
	 */
	public FloatMatrix multiply(float value) {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] *= value;
		}
		return this;
	}

	@Override
	public FloatMatrix copy() {
		final int len = this.rows * this.columns;
		final float[] copy = new float[len];
		System.arraycopy(this.data, 0, copy, 0, len);
		return new FloatMatrix(this.rows, this.columns, this.layout, copy);
	}

	@Override
	public FloatMatrix transpose() {
		final FloatMatrix tp = new FloatMatrix(this.columns, this.rows, this.layout);
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				tp.set(j, i, get(i, j));
			}
		}
		return tp;
	}

	@Override
	public FloatMatrix toColumnMajor() {
		if (this.layout.equals(Layout.COLUMN_MAJOR_ORDER)) {
			return this;
		}

		final FloatMatrix tp = this.transpose();
		return new FloatMatrix(this.rows, this.columns, Layout.COLUMN_MAJOR_ORDER, tp.data);
	}

	@Override
	public FloatMatrix toRowMajor() {
		if (this.layout.equals(Layout.ROW_MAJOR_ORDER)) {
			return this;
		}

		final FloatMatrix tp = this.transpose();
		return new FloatMatrix(this.rows, this.columns, Layout.ROW_MAJOR_ORDER, tp.data);
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + rows;
		hash = 31 * hash + columns;
		hash = 31 * hash + layout.hashCode();
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
		final FloatMatrix other = (FloatMatrix) obj;
		if (!Objects.equals(rows, other.rows)) {
			return false;
		}
		if (!Objects.equals(columns, other.columns)) {
			return false;
		}
		if (!Objects.equals(layout, other.layout)) {
			return false;
		}
		for (int i = 0, n = data.length; i < n; i++) {
			if (!Objects.equals(data[i], other.data[i])) {
				return false;
			}
		}
		return true;
	}

}
