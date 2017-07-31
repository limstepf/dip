package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedMatrix} with a single band of floats. This can
 * be any m-by-n matrix (of floats), or a single band of an image that is backed
 * by a {@code BufferedMatrix} (of floats), comparable to the
 * {@code BufferedImageGray} data type.
 */
public class BufferedMatrixFloat extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.BufferedMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-float");

	/**
	 * Creates a new data type for a {@code BufferedMatrix} with a single band
	 * of floats.
	 */
	public BufferedMatrixFloat() {
		super(ch.unifr.diva.dip.api.datastructures.BufferedMatrix.class);
	}

	@Override
	public String label() {
		return "Float";
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
