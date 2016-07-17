
package ch.unifr.diva.dip.api.datastructures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simple, marshallable 2D matrix class backed by a continous/linear array of
 * {@code boolean}s.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class BooleanMatrix extends Matrix {

	/**
	 * Continous booleans of the matrix.
	 */
	@XmlElement
	final public boolean[] data;

	@SuppressWarnings("unused")
	public BooleanMatrix() {
		this(0, 0, Layout.ROW_MAJOR_ORDER);
	}

	/**
	 * Creates a new, zeroed m-by-n matrix. Backed by a continuous/linear array
	 * in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 */
	public BooleanMatrix(int rows, int columns) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, new boolean[rows * columns]);
	}

	/**
	 * Creates a new, zeroed m-by-n matrix.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 */
	public BooleanMatrix(int rows, int columns, Layout layout) {
		this(rows, columns, layout, new boolean[rows * columns]);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array. Backed by a
	 * continuous/linear array in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param data continous data array of m times n booleans in row-major order.
	 */
	public BooleanMatrix(int rows, int columns, boolean... data) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, data);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 * @param data continous data array of m times n booleans.
	 */
	public BooleanMatrix(int rows, int columns, Layout layout, boolean... data) {
		super(rows, columns, layout);

		this.data = data;
	}

	/**
	 * Creates a matrix with the values and shape defined by the given 2D array.
	 * The data is assumed to be in row-major order.
	 *
	 * @param matrix the 2D array representation of the matrix.
	 */
	public BooleanMatrix(boolean[][] matrix) {
		super(matrix.length, matrix[0].length, Layout.ROW_MAJOR_ORDER);

		this.data = new boolean[this.rows * this.columns];

		int pos = 0;
		for (int i = 0; i < this.rows; i++) {
			final boolean[] row = matrix[i];

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
	public boolean get(int row, int column) {
		return get(index(row, column));
	}

	/**
	 * Returns the single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @return the element at the specified index.
	 */
	public boolean get(int index) {
		return this.data[index];
	}

	/**
	 * Converts the matrix to a 2D array of booleans in row-major order.
	 *
	 * @return a 2D array of booleans in row-major order.
	 */
	public boolean[][] getArray2D() {
		final boolean[][] a = new boolean[this.rows][this.columns];

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				a[i][j] = get(i, j);
			}
		}

		return a;
	}

	@Override
	protected String getPrintFormat(int numChar, int precision) {
		return "%" + numChar + "d ";
	}

	@Override
	public String get(int row, int column, String format) {
		return String.format(format, get(row, column) ? 1 : 0);
	}

	/**
	 * Sets a single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public BooleanMatrix set(int row, int column, boolean value) {
		return set(index(row, column), value);
	}

	/**
	 * Sets a single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public BooleanMatrix set(int index, boolean value) {
		this.data[index] = value;
		return this;
	}

	/**
	 * Sets all elements in the matrix to the new value.
	 *
	 * @param value the new value of all elements.
	 * @return the modified matrix.
	 */
	public BooleanMatrix fill(boolean value) {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] = value;
		}
		return this;
	}

	/**
	 * Inverts all (boolean) elements in the matrix.
	 *
	 * @return the modified matrix.
	 */
	public BooleanMatrix invert() {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] = !this.data[i];
		}
		return this;
	}

	@Override
	public BooleanMatrix copy() {
		final int len = this.rows * this.columns;
		final boolean[] copy = new boolean[len];
		System.arraycopy(this.data, 0, copy, 0, len);
		return new BooleanMatrix(this.rows, this.columns, this.layout, copy);
	}

	@Override
	public BooleanMatrix transpose() {
		final BooleanMatrix tp = new BooleanMatrix(this.columns, this.rows, this.layout);
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				tp.set(j, i, get(i, j));
			}
		}
		return tp;
	}

	@Override
	public BooleanMatrix toColumnMajor() {
		if (this.layout.equals(Layout.COLUMN_MAJOR_ORDER)) {
			return this;
		}

		final BooleanMatrix tp = this.transpose();
		return new BooleanMatrix(this.rows, this.columns, Layout.COLUMN_MAJOR_ORDER, tp.data);
	}

	@Override
	public BooleanMatrix toRowMajor() {
		if (this.layout.equals(Layout.ROW_MAJOR_ORDER)) {
			return this;
		}

		final BooleanMatrix tp = this.transpose();
		return new BooleanMatrix(this.rows, this.columns, Layout.ROW_MAJOR_ORDER, tp.data);
	}

}
