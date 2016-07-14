package ch.unifr.diva.dip.api.datastructures;

/**
 * A kernel with coefficients in double-precision floating points.
 */
public class DoubleKernel extends KernelBase<DoubleMatrix> {

	/**
	 * Creates a new kernel with float coefficients in the shape of the given
	 * matrix.
	 *
	 * @param matrix the matrix with the kernel coefficients.
	 */
	public DoubleKernel(DoubleMatrix matrix) {
		super(matrix);
	}

	@Override
	public float getValueFloat(int column, int row) {
		return (float) this.matrix.data[index(column, row)];
	}

	@Override
	public double getValueDouble(int column, int row) {
		return this.matrix.data[index(column, row)];
	}
}
