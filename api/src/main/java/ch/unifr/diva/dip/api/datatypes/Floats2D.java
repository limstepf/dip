package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a 2D array of floats.
 */
public class Floats2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Floats2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/floats2D");

	/**
	 * Creates a new data type for a 2D array of floats.
	 */
	public Floats2D() {
		super(ch.unifr.diva.dip.api.datastructures.Floats2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
