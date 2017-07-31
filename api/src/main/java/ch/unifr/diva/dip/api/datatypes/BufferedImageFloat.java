package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedMatrix} with single-precision floating points.
 * This usually implies a single band, but multiband images are fine too (taking
 * the first band into account, unless further specified, e.g. by some
 * parameter). No color space is specified.
 */
public class BufferedImageFloat extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.BufferedMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-float");

	/**
	 * Creates a new data type for a {@code BufferedMatrix} with
	 * single-precision floating points.
	 */
	public BufferedImageFloat() {
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
