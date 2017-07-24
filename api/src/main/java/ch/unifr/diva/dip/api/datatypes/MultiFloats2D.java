package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code MultiFloats2D}.
 */
public class MultiFloats2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.MultiFloats2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/multi-floats2d");

	/**
	 * Creates a new data type for {@code MultiFloats2D}.
	 */
	public MultiFloats2D() {
		super(ch.unifr.diva.dip.api.datastructures.MultiFloats2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
