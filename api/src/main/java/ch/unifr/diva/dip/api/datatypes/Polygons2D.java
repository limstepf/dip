package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a list of {@code Polygons2D}.
 */
public class Polygons2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Polygon2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-polygon2d");

	/**
	 * Creates a new data type for a list of {@code Polygons2D}.
	 */
	public Polygons2D() {
		super(ch.unifr.diva.dip.api.datastructures.Polygon2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

	@Override
	public CollectionType getCollectionType() {
		return CollectionType.LIST;
	}

}
