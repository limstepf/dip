package ch.unifr.diva.dip.api.datastructures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A kernel with coefficients in double-precision floating points.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DoubleKernel extends Kernel<DoubleMatrix> {

	@SuppressWarnings("unused")
	public DoubleKernel() {
		this(new DoubleMatrix());
	}

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
