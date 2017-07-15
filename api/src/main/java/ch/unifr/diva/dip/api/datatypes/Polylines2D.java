package ch.unifr.diva.dip.api.datatypes;

import ch.unifr.diva.dip.api.datastructures.Polyline2D;
import javafx.scene.input.DataFormat;

/**
 * Data type for a list of {@code Polylines2D}.
 */
public class Polylines2D extends AbstractDataType<Polyline2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-polyline2d");

	/**
	 * Creates a new data type for a list of {@code Polylines2D}.
	 */
	public Polylines2D() {
		super(Polyline2D.class);
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
