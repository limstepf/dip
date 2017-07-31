package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a 2D polygon.
 */
public class Polygon2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Polygon2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/polygon-2d");

	/**
	 * Creates a new data type for a 2D polygon.
	 */
	public Polygon2D() {
		super(ch.unifr.diva.dip.api.datastructures.Polygon2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
