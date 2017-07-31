package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedMatrix} with a Lab color space.
 */
public class BufferedImageLab extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.BufferedMatrix> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-lab");

	/**
	 * Creates a new data type for a {@code BufferedMatrix} with a Lab color
	 * space.
	 */
	public BufferedImageLab() {
		super(ch.unifr.diva.dip.api.datastructures.BufferedMatrix.class);
	}

	@Override
	public String label() {
		return "Lab";
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
