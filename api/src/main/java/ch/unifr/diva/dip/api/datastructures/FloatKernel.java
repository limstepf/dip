package ch.unifr.diva.dip.api.datastructures;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A kernel with coefficients in floating point precision.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class FloatKernel extends Kernel<FloatMatrix> {

	@SuppressWarnings("unused")
	public FloatKernel() {
		this(new FloatMatrix());
	}

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
