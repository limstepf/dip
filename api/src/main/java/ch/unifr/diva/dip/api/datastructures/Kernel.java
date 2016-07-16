package ch.unifr.diva.dip.api.datastructures;

import java.awt.Rectangle;

/**
 * KernelBase is a simple wrapper around a matrix. For the kernel we shift the
 * bounds (or all elements in the matrix) by {@code -width/2}, and
 * {@code -height/2} respectively (integer division!), s.t. the center of the
 * kernel (or key element) falls on row/columns zero (given we're dealing with a
 * kernel of odd size). The column and row indices to access the kernel
 * coefficients are now allowed to be negative.
 *
 * <p>
 * Why not directly use the matrix? Because it's way nicer to work with, e.g.
 * for a ConvolutionOp. That's why. <br />
 *
 * Just make sure to use a {@code RasterScanner} to iterate over all kernel
 * coefficients, or if you have to write a for-loop then start at
 * {@code bounds().x} (and {@code bounds().y}) and then watch out to not
 * overshoot: {@code x < bounds().width} will give you invalid coordinates for a
 * bunch of coefficients too much - it really should be:
 * {@code x < bounds().width - 1}. Same for the row/height.
 *
 * @param <T> class of the wrapped matrix.
 */
public abstract class KernelBase<T extends MatrixBase> {

	protected final T matrix;
	protected final Rectangle bounds;

	/**
	 * Creates a new kernel in the shape (and precision) of the given matrix.
	 *
	 * @param matrix the matrix containing the kernel coefficients.
	 */
	public KernelBase(T matrix) {
		this.matrix = matrix.toRowMajor();
		this.bounds = new Rectangle(
				this.matrix.columns / -2,
				this.matrix.rows / -2,
				this.matrix.columns,
				this.matrix.rows
		);
	}

	/**
	 * Returns the wrapped matrix with the kernel coefficients.
	 *
	 * @return the matrix containing the kernel coefficients.
	 */
	public T matrix() {
		return this.matrix;
	}

	/**
	 * Returns the index into the continuous/linear array of the matrix.
	 *
	 * @param column the X coordinate of the kernel coefficient.
	 * @param row the Y coordinate of the kernel coefficient.
	 * @return the index into the array of the matrix.
	 */
	protected int index(int column, int row) {
		return (row - bounds().y) * width() + (column - bounds().x);
	}

	/**
	 * Returns the bounds of the kernel.
	 *
	 * @return the bounds of the kernel.
	 */
	public Rectangle bounds() {
		return this.bounds;
	}

	/**
	 * Returns the width of the kernel.
	 *
	 * @return the width of the kernel.
	 */
	public int width() {
		return this.bounds.width;
	}

	/**
	 * Returns the height of the kernel.
	 *
	 * @return the height of the kernel.
	 */
	public int height() {
		return this.bounds.height;
	}

	/**
	 * Returns the kernel coefficient as a float.
	 *
	 * @param column the X coordinate of the kernel coefficient.
	 * @param row the Y coordinate of the kernel coefficient.
	 * @return the kernel coefficient.
	 */
	public abstract float getValueFloat(int column, int row);

	/**
	 * Returns the kernel coefficient as a double.
	 *
	 * @param column the X coordinate of the kernel coefficient.
	 * @param row the Y coordinate of the kernel coefficient.
	 * @return the kernel coefficient.
	 */
	public abstract double getValueDouble(int column, int row);

}
