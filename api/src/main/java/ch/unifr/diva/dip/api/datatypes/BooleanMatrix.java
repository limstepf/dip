package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BooleanMatrix} of any shape.
 */
public class BooleanMatrix implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.BooleanMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/boolean-matrix");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
