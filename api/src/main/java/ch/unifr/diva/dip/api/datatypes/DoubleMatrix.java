package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code DoubleMatrix} of any shape.
 */
public class DoubleMatrix implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.DoubleMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/double-matrix");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
