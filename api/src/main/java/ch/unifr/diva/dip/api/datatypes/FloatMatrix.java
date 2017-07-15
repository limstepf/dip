package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code FloatMatrix} of any shape.
 */
public class FloatMatrix extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.FloatMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/float-matrix");

	/**
	 * Creates a new data type for a {@code FloatMatrix}.
	 */
	public FloatMatrix() {
		super(ch.unifr.diva.dip.api.datastructures.FloatMatrix.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
