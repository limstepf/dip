package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code Polylines2D}.
 */
public class Polylines2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Polylines2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/polylines-2d");

	/**
	 * Creates a new data type for {@code Polylines2D}.
	 */
	public Polylines2D() {
		super(ch.unifr.diva.dip.api.datastructures.Polylines2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
