package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedImage} with an XYZ (or CIE XYZ) color space.
 */
public class BufferedImageXyz implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.BufferedMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-xyz");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
