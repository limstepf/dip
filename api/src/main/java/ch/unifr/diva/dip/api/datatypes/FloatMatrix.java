package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code FloatMatrix} of any shape.
 */
public class FloatMatrix implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.FloatMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/float-matrix");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
