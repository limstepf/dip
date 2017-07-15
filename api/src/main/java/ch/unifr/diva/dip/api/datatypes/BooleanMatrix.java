package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BooleanMatrix} of any shape.
 */
public class BooleanMatrix extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.BooleanMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/boolean-matrix");

	/**
	 * Creates a new data type for a {@code BooleanMatrix}.
	 */
	public BooleanMatrix() {
		super(ch.unifr.diva.dip.api.datastructures.BooleanMatrix.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
