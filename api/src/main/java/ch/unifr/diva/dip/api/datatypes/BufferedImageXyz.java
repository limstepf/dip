package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedMatrix} with an XYZ (or CIE XYZ) color space.
 */
public class BufferedImageXyz extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.BufferedMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-xyz");

	/**
	 * Creates a new data type for a {@code BufferedMatrix} with an XYZ (or CIE
	 * XYZ) color space.
	 */
	public BufferedImageXyz() {
		super(ch.unifr.diva.dip.api.datastructures.BufferedMatrix.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
