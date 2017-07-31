package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedMatrix} with an YUV color space.
 */
public class BufferedImageYuv extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.BufferedMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-yuv");

	/**
	 * Creates a new data type for a {@code BufferedMatrix} with an YUV color
	 * space.
	 */
	public BufferedImageYuv() {
		super(ch.unifr.diva.dip.api.datastructures.BufferedMatrix.class);
	}

	@Override
	public String label() {
		return "YUV";
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
