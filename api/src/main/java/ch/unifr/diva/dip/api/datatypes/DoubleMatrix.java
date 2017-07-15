package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code DoubleMatrix} of any shape.
 */
public class DoubleMatrix extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.DoubleMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/double-matrix");

	/**
	 * Creates a new data type for a {@code DoubleMatrix}.
	 */
	public DoubleMatrix() {
		super(ch.unifr.diva.dip.api.datastructures.DoubleMatrix.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
