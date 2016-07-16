package ch.unifr.diva.dip.api.datastructures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple, marshallable 2D matrix class backed by a continous/linear array of
 * {@code double}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DoubleMatrix extends Matrix {

	/**
	 * Continous doubles of the matrix.
	 */
	@XmlElement
	final public double[] data;

	@SuppressWarnings("unused")
	public DoubleMatrix() {
		this(0, 0, Layout.ROW_MAJOR_ORDER);
	}

	/**
	 * Creates a new, zeroed m-by-n matrix. Backed by a continuous/linear array
	 * in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 */
	public DoubleMatrix(int rows, int columns) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, new double[rows * columns]);
	}

	/**
	 * Creates a new, zeroed m-by-n matrix.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 */
	public DoubleMatrix(int rows, int columns, Layout layout) {
		this(rows, columns, layout, new double[rows * columns]);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array. Backed by a
	 * continuous/linear array in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param data continous data array of m times n doubles in row-major order.
	 */
	public DoubleMatrix(int rows, int columns, double... data) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, data);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 * @param data continous data array of m times n doubles.
	 */
	public DoubleMatrix(int rows, int columns, Layout layout, double... data) {
		super(rows, columns, layout);

		this.data = data;
	}

	/**
	 * Creates a matrix with the values and shape defined by the given 2D array.
	 * The data is assumed to be in row-major order.
	 *
	 * @param matrix the 2D array representation of the matrix.
	 */
	public DoubleMatrix(double[][] matrix) {
		super(matrix.length, matrix[0].length, Layout.ROW_MAJOR_ORDER);

		this.data = new double[this.rows * this.columns];

		int pos = 0;
		for (int i = 0; i < this.rows; i++) {
			final double[] row = matrix[i];

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
	public double get(int row, int column) {
		return get(index(row, column));
	}

	/**
	 * Returns the single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @return the element at the specified index.
	 */
	public double get(int index) {
		return this.data[index];
	}

	/**
	 * Converts the matrix to a 2D array of doubles in row-major order.
	 *
	 * @return a 2D array of doubles in row-major order.
	 */
	public double[][] getArray2D() {
		final double[][] a = new double[this.rows][this.columns];

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

		final float[] floats = new float[this.data.length];
		for (int i = 0; i < this.data.length; i++) {
			floats[i] = (float) this.data[i];
		}

		return new java.awt.image.Kernel(this.columns, this.rows, floats);
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
	public DoubleMatrix set(int row, int column, double value) {
		return set(index(row, column), value);
	}

	/**
	 * Sets a single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public DoubleMatrix set(int index, double value) {
		this.data[index] = value;
		return this;
	}

	/**
	 * Sets all elements in the matrix to the new value.
	 *
	 * @param value the new value of all elements.
	 * @return the modified matrix.
	 */
	public DoubleMatrix fill(double value) {
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
	public DoubleMatrix add(double value) {
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
	public DoubleMatrix multiply(double value) {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] *= value;
		}
		return this;
	}

	@Override
	public DoubleMatrix copy() {
		final int len = this.rows * this.columns;
		final double[] copy = new double[len];
		System.arraycopy(this.data, 0, copy, 0, len);
		return new DoubleMatrix(this.rows, this.columns, this.layout, copy);
	}

	@Override
	public DoubleMatrix transpose() {
		final DoubleMatrix tp = new DoubleMatrix(this.columns, this.rows, this.layout);
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				tp.set(j, i, get(i, j));
			}
		}
		return tp;
	}

	@Override
	public DoubleMatrix toColumnMajor() {
		if (this.layout.equals(Layout.COLUMN_MAJOR_ORDER)) {
			return this;
		}

		final DoubleMatrix tp = this.transpose();
		return new DoubleMatrix(this.rows, this.columns, Layout.COLUMN_MAJOR_ORDER, tp.data);
	}

	@Override
	public DoubleMatrix toRowMajor() {
		if (this.layout.equals(Layout.ROW_MAJOR_ORDER)) {
			return this;
		}

		final DoubleMatrix tp = this.transpose();
		return new DoubleMatrix(this.rows, this.columns, Layout.ROW_MAJOR_ORDER, tp.data);
	}

}
