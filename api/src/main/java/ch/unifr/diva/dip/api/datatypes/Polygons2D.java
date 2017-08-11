package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code Polygons2D}.
 */
public class Polygons2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Polygons2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/polygons-2d");

	/**
	 * Creates a new data type for {@code Polygons2D}.
	 */
	public Polygons2D() {
		super(ch.unifr.diva.dip.api.datastructures.Polygons2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
