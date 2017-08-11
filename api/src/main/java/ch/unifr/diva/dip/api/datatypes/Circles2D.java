package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code Circles2D}.
 */
public class Circles2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Circles2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/circles-2d");

	/**
	 * Creates a new data type for {@code Circles2D}.
	 */
	public Circles2D() {
		super(ch.unifr.diva.dip.api.datastructures.Circles2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
