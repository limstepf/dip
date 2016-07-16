package ch.unifr.diva.dip.api.datastructures;

/**
 * A kernel with coefficients in floating point precision.
 */
public class FloatKernel extends Kernel<FloatMatrix> {

	/**
	 * Creates a new kernel with float coefficients in the shape of the given
	 * matrix.
	 *
	 * @param matrix the matrix with the kernel coefficients.
	 */
	public FloatKernel(FloatMatrix matrix) {
		super(matrix);
	}

	@Override
	public float getValueFloat(int column, int row) {
		return this.matrix.data[index(column, row)];
	}

	@Override
	public double getValueDouble(int column, int row) {
		return this.matrix.data[index(column, row)];
	}
}
