package ch.unifr.diva.dip.api.datastructures;

import java.util.Arrays;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * A simple, marshallable 2D matrix class backed by a continous/linear array of
 * {@code String}s.
 *
 * <p>
 * A matrix with strings, eh? Why, yes! If we'd like to have a neat matrix
 * parameter, we're better off having it based on strings, s.t. we can use math.
 * expressions to define the single elements. Later on we can then still
 * retrieve a {@code FloatMatrix} or {@code DoubleMatrix}.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class StringMatrix extends Matrix {

	/**
	 * Continous Strings of the matrix.
	 */
	@XmlElement
	final public String[] data;

	/**
	 * The null value is returned for null strings if evaluated. The default is
	 * '0', but can be as well set to {@code Double.NaN} (or what not) if that
	 * suits you better.
	 */
	@XmlAttribute
	private double nullValue = 0;

	@SuppressWarnings("unused")
	public StringMatrix() {
		this(0, 0, Layout.ROW_MAJOR_ORDER);
	}

	/**
	 * Creates a new m-by-n matrix. Backed by a continuous/linear array in
	 * row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 */
	public StringMatrix(int rows, int columns) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, new String[rows * columns]);
	}

	/**
	 * Creates a new m-by-n matrix.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 */
	public StringMatrix(int rows, int columns, Layout layout) {
		this(rows, columns, layout, new String[rows * columns]);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array. Backed by a
	 * continuous/linear array in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param data continous data array of m times n strings in row-major order.
	 */
	public StringMatrix(int rows, int columns, String... data) {
		this(rows, columns, Layout.ROW_MAJOR_ORDER, data);
	}

	/**
	 * Creates a m-by-n matrix backed by the given data array.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param layout layout used by the continuous/linear array.
	 * @param data continous data array of m times n strings.
	 */
	public StringMatrix(int rows, int columns, Layout layout, String... data) {
		super(rows, columns, layout);

		this.data = data;
	}

	/**
	 * Creates a matrix with the values and shape defined by the given 2D array.
	 * The data is assumed to be in row-major order.
	 *
	 * @param matrix the 2D array representation of the matrix.
	 */
	public StringMatrix(String[][] matrix) {
		super(matrix.length, matrix[0].length, Layout.ROW_MAJOR_ORDER);

		this.data = new String[this.rows * this.columns];

		int pos = 0;
		for (int i = 0; i < this.rows; i++) {
			final String[] row = matrix[i];

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
	 * Sets the value for unset/uninitialized null strings. The default null
	 * value is '0', but {@code Double.NaN} could make more sense...
	 *
	 * @param value the value returned for null strings.
	 */
	public void setNullValue(double value) {
		this.nullValue = value;
	}

	/**
	 * Returns the null value used upon evaluating null strings.
	 *
	 * @return the null value.
	 */
	public double getNullValue() {
		return this.nullValue;
	}

	/**
	 * Returns the single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @return the element at the specified row/column.
	 */
	public String get(int row, int column) {
		return get(index(row, column));
	}

	/**
	 * Returns the single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @return the element at the specified index.
	 */
	public String get(int index) {
		return this.data[index];
	}

	protected double evalDouble(String expression) {
		if (expression == null) {
			return this.nullValue;
		}

		try {
			Expression exp = new ExpressionBuilder(expression.toLowerCase()).build();
			if (exp.validate().isValid()) {
				return exp.evaluate();
			}
		} catch (Exception ex) {
			// NaN it is then...
		}

		return Double.NaN;
	}

	protected float evalFloat(String expression) {
		return (float) evalDouble(expression);
	}

	protected boolean evalBoolean(String expression) {
		if (expression == null) {
			return false;
		}

		if (!expression.isEmpty()) {
			final char c = Character.toLowerCase(expression.charAt(0));
			switch (c) {
				case 't':
					return true;
				case 'f':
					return false;
				case '1':
					return true;
				case '0':
					return false;
			}
		}

		return evalDouble(expression) > 0;
	}

	/**
	 * Evaluates and returns the single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @return the double value represented by the element, or Double.NaN if the
	 * expression is invalid.
	 */
	public double getDouble(int row, int column) {
		return evalDouble(get(row, column));
	}

	/**
	 * Evaluates and returns the single element at the specified index (linear
	 * indexing).
	 *
	 * @param index the index of the element.
	 * @return the double value represented by the element, or Double.NaN if the
	 * expression is invalid.
	 */
	public double getDouble(int index) {
		return evalDouble(get(index));
	}

	/**
	 * Evaluates and returns the single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @return the float value represented by the element, or Float.NaN if the
	 * expression is invalid.
	 */
	public float getFloat(int row, int column) {
		return evalFloat(get(row, column));
	}

	/**
	 * Evaluates and returns the single element at the specified index (linear
	 * indexing).
	 *
	 * @param index the index of the element.
	 * @return the float value represented by the element, or Float.NaN if the
	 * expression is invalid.
	 */
	public float getFloat(int index) {
		return evalFloat(get(index));
	}

	/**
	 * Evaluates and returns the single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @return the boolean value represented by the element. Anything larger
	 * than
	 *
	 */
	public boolean getBoolean(int row, int column) {
		return evalBoolean(get(row, column));
	}

	/**
	 * Evaluates and returns the single element at the specified index (linear
	 * indexing).
	 *
	 * @param index the index of the element.
	 * @return the float value represented by the element, or Float.NaN if the
	 * expression is invalid.
	 */
	public boolean getBoolean(int index) {
		return evalBoolean(get(index));
	}

	/**
	 * Converts the matrix to a {@code DoubleMatrix} by evaluating the string
	 * elements as mathematical expressions.
	 *
	 * @return a matrix of doubles.
	 */
	public DoubleMatrix getDoubleMatrix() {
		final DoubleMatrix mat = new DoubleMatrix(
				this.rows, this.columns, this.layout
		);

		for (int i = 0; i < this.data.length; i++) {
			mat.data[i] = getDouble(i);
		}

		return mat;
	}

	/**
	 * Converts the matrix to a {@code FloatMatrix} by evaluating the string
	 * elements as mathematical expressions.
	 *
	 * @return a matrix of floats.
	 */
	public FloatMatrix getFloatMatrix() {
		final FloatMatrix mat = new FloatMatrix(
				this.rows, this.columns, this.layout
		);

		for (int i = 0; i < this.data.length; i++) {
			mat.data[i] = getFloat(i);
		}

		return mat;
	}

	/**
	 * Converts the matrix to a {@code BooleanMatrix}. This is done by either
	 * evaluating the string elements as mathematical expressions ({@code x > 0}
	 * is considered true), or by checking the first char which can be either
	 * {@code 't'} or {@code 'f'} (not case sensitive).
	 *
	 * @return a matrix of booleans.
	 */
	public BooleanMatrix getBooleanMatrix() {
		final BooleanMatrix mat = new BooleanMatrix(
				this.rows, this.columns, this.layout
		);

		for (int i = 0; i < this.data.length; i++) {
			mat.data[i] = getBoolean(i);
		}

		return mat;
	}

	/**
	 * Converts the matrix to a 2D array of doubles in row-major order.
	 *
	 * @return a 2D array of doubles in row-major order.
	 */
	public String[][] getArray2D() {
		final String[][] a = new String[this.rows][this.columns];

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				a[i][j] = get(i, j);
			}
		}

		return a;
	}

	@Override
	public String get(int row, int column, String format) {
		return String.format(format, get(row, column));
	}

	@Override
	protected String getPrintFormat(int numChar, int precision) {
		return "%" + numChar + "s ";
	}

	/**
	 * Sets a single element at the specified row/column.
	 *
	 * @param row the row of the element.
	 * @param column the column of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public StringMatrix set(int row, int column, String value) {
		return set(index(row, column), value);
	}

	/**
	 * Sets a single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @param value the new value of the element.
	 * @return the modified matrix.
	 */
	public StringMatrix set(int index, String value) {
		this.data[index] = value;
		return this;
	}

	/**
	 * Sets all elements in the matrix to the new value.
	 *
	 * @param value the new value of all elements.
	 * @return the modified matrix.
	 */
	public StringMatrix fill(String value) {
		for (int i = 0; i < this.data.length; i++) {
			this.data[i] = value;
		}
		return this;
	}

	@Override
	public StringMatrix copy() {
		final int len = this.rows * this.columns;
		final String[] copy = new String[len];
		System.arraycopy(this.data, 0, copy, 0, len);
		return new StringMatrix(this.rows, this.columns, this.layout, copy);
	}

	@Override
	public StringMatrix transpose() {
		final StringMatrix tp = new StringMatrix(this.columns, this.rows, this.layout);
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				tp.set(j, i, get(i, j));
			}
		}
		return tp;
	}

	@Override
	public StringMatrix toColumnMajor() {
		if (this.layout.equals(Layout.COLUMN_MAJOR_ORDER)) {
			return this;
		}

		final StringMatrix tp = this.transpose();
		return new StringMatrix(this.rows, this.columns, Layout.COLUMN_MAJOR_ORDER, tp.data);
	}

	@Override
	public StringMatrix toRowMajor() {
		if (this.layout.equals(Layout.ROW_MAJOR_ORDER)) {
			return this;
		}

		final StringMatrix tp = this.transpose();
		return new StringMatrix(this.rows, this.columns, Layout.ROW_MAJOR_ORDER, tp.data);
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = 31 * hash + rows;
		hash = 31 * hash + columns;
		hash = 31 * hash + layout.hashCode();
		hash = 31 * hash + Double.hashCode(nullValue);
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
		final StringMatrix other = (StringMatrix) obj;
		if (!Objects.equals(rows, other.rows)) {
			return false;
		}
		if (!Objects.equals(columns, other.columns)) {
			return false;
		}
		if (!Objects.equals(layout, other.layout)) {
			return false;
		}
		if (!Objects.equals(nullValue, other.nullValue)) {
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
