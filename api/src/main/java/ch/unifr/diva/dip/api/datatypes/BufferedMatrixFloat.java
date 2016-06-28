package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedMatrix} with a single band of floats. This can be any m-by-n
 * matrix (of floats), or a single band of an image that is backed by a
 * {@code BufferedMatrix} (of floats), comparable to the
 * {@code BufferedImageGray} data type.
 */
public class BufferedMatrixFloat implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.imaging.BufferedMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-float");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
