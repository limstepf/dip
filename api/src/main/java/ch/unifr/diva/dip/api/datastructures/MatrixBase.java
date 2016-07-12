package ch.unifr.diva.dip.api.datastructures;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base class for real 2D matrices backed by a continuous/linear array. Already
 * provides the dimensions of the matrix, as well as the internal layout.
 *
 * <ul>
 * <li>The layout of the continous/linear array can be either in column-major
 * (Fortran) or row-major-order (C), s.t. conversion from and to more powerful
 * matrix/linear algebra classes (e.g. EJML, JBLAS, ...) should be fast and
 * efficient. Ideally the same chunk of memory can be used/passed.<br />
 *
 * In other words: the goal here is not to reinvent the wheel (of linear
 * algebra), but to have common matrix classes (that can be used to communicate
 * between DIP processors) that nicely bridge to more appropriate libraries.
 * </li>
 *
 * <li>Subclasses of {@code RealMatrix} are intended to be marshallable by JAXB
 * s.t. we can use them to back parameters (e.g. for a kernel, convolution
 * matrix, or mask). And as such, only small matrices should be stored/passed
 * encoded by XML.</li>
 * </ul>
 */
public abstract class MatrixBase {

	/**
	 * The layout defines how the matrix is stored in the continuous/linear
	 * array.
	 */
	public static enum Layout {

		/**
		 * Column-major order. Used in Fortran, OpenGL, Matlab, R, Julia, GNU
		 * Octave.
		 */
		COLUMN_MAJOR_ORDER() {

					@Override
					public int index(int row, int column, int rows, int columns) {
						return column * rows + row;
					}

				},
		/**
		 * Row-major order (default). Used in C/C++ (C-style arrays), Pascal,
		 * Python, Mathematica.
		 */
		ROW_MAJOR_ORDER() {

					@Override
					public int index(int row, int column, int rows, int columns) {
						return row * columns + column;
					}

				};

		/**
		 * Returns the index (or offset) into the continuous/linear array of the
		 * specified element.
		 *
		 * @param row the row of the element.
		 * @param column the column of the element.
		 * @param rows number of rows in the matrix.
		 * @param columns number of columns in the matrix
		 * @return the index (or offset) of the specified element.
		 */
		public abstract int index(int row, int column, int rows, int columns);
	}

	/**
	 * Number of rows.
	 */
	@XmlAttribute
	final public int rows;

	/**
	 * Number of columns.
	 */
	@XmlAttribute
	final public int columns;

	/**
	 * Layout used for continuous/linear array.
	 */
	@XmlAttribute
	final public Layout layout;

	/**
	 * Creates a m-by-n matrix.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 */
	public MatrixBase(int rows, int columns, Layout layout) {
		this.rows = rows;
		this.columns = columns;
		this.layout = layout;
	}

	/**
	 * Returns the index (or offset) into the continuous/linear array of the
	 * specified element.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @return the linear index (or offset) of the specified element.
	 */
	public int index(int row, int column) {
		return this.layout.index(row, column, this.rows, this.columns);
	}

	/**
	 * Returns the number of elments in the matrix. That is the number of rows
	 * times the number of columns.
	 *
	 * @return the number of elements in the matrix.
	 */
	public int numElements() {
		return this.rows * this.columns;
	}

	/**
	 * Checks whether the matrix is square or not.
	 *
	 * @return True if the matrix is square, False otherwise.
	 */
	public boolean isSquare() {
		return this.rows == this.columns;
	}

	/**
	 * Checks whether the matrix is a vector.
	 *
	 * @return True if the matrix is a vector, False otherwise.
	 */
	public boolean isVector() {
		return isRowVector() || isColumnVector();
	}

	/**
	 * Checks whether the matrix is a row vector.
	 *
	 * @return True if the matrix is a row vector, False otherwise.
	 */
	public boolean isRowVector() {
		return this.rows == 1;
	}

	/**
	 * Checks whether the matrix is a column vector.
	 *
	 * @return True if the matrix is a column vector, False otherwise.
	 */
	public boolean isColumnVector() {
		return this.columns == 1;
	}

	/**
	 * Checks whether a matrix has the same dimensions as this one.
	 *
	 * @param mat another matrix.
	 * @return True if both matrices have the same dimensions, False otherwise.
	 */
	public boolean equalSize(MatrixBase mat) {
		return equalSize(this, mat);
	}

	/**
	 * Checks whether two matrices have the same dimensions.
	 *
	 * @param a the first matrix.
	 * @param b the second matrix.
	 * @return True if both matrices have the same dimensions, False otherwise.
	 */
	public static boolean equalSize(MatrixBase a, MatrixBase b) {
		return (a.rows == b.rows) && (a.columns == b.columns);
	}

	/**
	 * Returns a copy (or clone) of this matrix.
	 *
	 * @param <T> subclass of {@code MatrixBase}.
	 * @return a copy of this matrix.
	 */
	public abstract <T extends MatrixBase> T copy();

	/**
	 * Returns a transposed copy of this matrix.
	 *
	 * @param <T> subclass of {@code MatrixBase}.
	 * @return a transposed copy of this matrix.
	 */
	public abstract <T extends MatrixBase> T transpose();

	/**
	 * Returns the matrix in column major order.
	 *
	 * @param <T> subclass of {@code MatrixBase}.
	 * @return the matrix in column major order.
	 */
	public abstract <T extends MatrixBase> T toColumnMajor();

	/**
	 * Returns the matrix in row major order.
	 *
	 * @param <T> subclass of {@code MatrixBase}.
	 * @return the matrix in row major order.
	 */
	public abstract <T extends MatrixBase> T toRowMajor();

	/**
	 * Returns a pretty printed string representation of the matrix.
	 *
	 * @param numChar total number of characters of the string representation of
	 * a single element.
	 * @param precision number of characters after the period.
	 * @return a string representation of the matrix.
	 */
	public String toString(int numChar, int precision) {
		return toString(getPrintFormat(numChar, precision));
	}

	/**
	 * Returns a pretty printed string representation of the matrix.
	 *
	 * @param format the format to format a single element (e.g. {@code "%f"}).
	 * @return a string representation of the matrix.
	 */
	public String toString(String format) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				sb.append(get(i, j, format));
			}
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * Returns a string format to format a single element of the matrix.
	 *
	 * @param numChar total number of characters of the string representation of
	 * a single element.
	 * @param precision number of characters after the period.
	 * @return the format to format a single element.
	 */
	protected String getPrintFormat(int numChar, int precision) {
		return "%" + numChar + "." + precision + "f ";
	}

	/**
	 * Returns a formatted string representation of a single element.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @param format the format to format a single element (e.g. {@code "%f"}).
	 * @return formatted string represetnation of the element at the given
	 * row/column.
	 */
	public abstract String get(int row, int column, String format);

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "rows=" + this.rows
				+ ", columns=" + this.rows
				+ ", layout=" + this.layout
				+ "}";
	}

}
