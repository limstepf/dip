package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a 1D array of floats.
 */
public class Floats1D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Floats1D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/floats1D");

	/**
	 * Creates a new data type for a 1D array of floats.
	 */
	public Floats1D() {
		super(ch.unifr.diva.dip.api.datastructures.Floats1D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
