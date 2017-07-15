package ch.unifr.diva.dip.api.datastructures;

/**
 * A mask is a special type of kernel backed by a {@code BooleanMatrix}.
 * Primarily used to mark the neighbours of a pixel in some pattern (e.g. for a
 * rank filter), where {@code true} means inclusion, {@code false} exclusion. As
 * with a kernel, a mask is centered, s.t. rows and columns may be negative.
 */
public class Mask extends Kernel<BooleanMatrix> {

	/**
	 * Creates a new, empty m-by-n mask with specified shape.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 */
	public Mask(int rows, int columns) {
		this(new BooleanMatrix(rows, columns));
	}

	/**
	 * Creates a new m-by-n mask with specified shape, and initialized with
	 * given data in row-major order.
	 *
	 * @param rows number of rows (m).
	 * @param columns number of columns (n).
	 * @param data data of the mask in row-major order.
	 */
	public Mask(int rows, int columns, boolean... data) {
		this(new BooleanMatrix(rows, columns, Matrix.Layout.ROW_MAJOR_ORDER, data));
	}

	/**
	 * Creates a new mask with the values and shape defined by the given 2D
	 * array. The data is assumed to be in row-major order.
	 *
	 * @param mask the 2D array representation of the mask.
	 */
	public Mask(boolean[][] mask) {
		this(new BooleanMatrix(mask));
	}

	/**
	 * Creates a new mask backed by the given matrix.
	 *
	 * @param matrix boolean matrix representing the mask.
	 */
	public Mask(BooleanMatrix matrix) {
		super(matrix);
	}

	/**
	 * Returns the single element at the specified row/column.
	 *
	 * @param x the X coordinate of the mask.
	 * @param y the Y coordinate of the mask.
	 * @return the element at the specified row/column.
	 */
	public boolean get(int x, int y) {
		return this.matrix.data[index(x, y)];
	}

	/**
	 * Returns the single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @return the element at the specified index.
	 */
	public boolean get(int index) {
		return this.matrix.data[index];
	}

	/**
	 * Returns the number of booleans set to {@code true} in this mask.
	 *
	 * @return the number of booleans set to {@code true} in this mask.
	 */
	public int cardinality() {
		int c = 0;

		for (int i = 0; i < this.matrix.data.length; i++) {
			if (this.matrix.data[i]) {
				c++;
			}
		}

		return c;
	}

	/**
	 * Sets a single element at the specified row/column.
	 *
	 * @param x the X coordinate of the mask.
	 * @param y the Y coordinate of the mask.
	 * @param value the new value of the element.
	 * @return the modified mask.
	 */
	public Mask set(int x, int y, boolean value) {
		this.matrix.data[index(x, y)] = value;
		return this;
	}

	/**
	 * Sets a single element at the specified index (linear indexing).
	 *
	 * @param index the index of the element.
	 * @param value the new value of the element.
	 * @return the modified mask.
	 */
	public Mask set(int index, boolean value) {
		this.matrix.data[index] = value;
		return this;
	}

	/**
	 * Sets all elements in the mask to the given value.
	 *
	 * @param value the value to fill the mask with.
	 * @return the modified mask.
	 */
	public Mask fill(boolean value) {
		this.matrix.fill(value);
		return this;
	}

	/**
	 * Inverts the mask.
	 *
	 * @return the modified mask.
	 */
	public Mask invert() {
		this.matrix.invert();
		return this;
	}

	@Override
	public float getValueFloat(int column, int row) {
		return this.matrix.data[index(column, row)] ? 1.0f : 0.0f;
	}

	@Override
	public double getValueDouble(int column, int row) {
		return this.matrix.data[index(column, row)] ? 1 : 0;
	}

}
