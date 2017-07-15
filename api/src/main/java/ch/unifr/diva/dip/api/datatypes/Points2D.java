package ch.unifr.diva.dip.api.datatypes;

import ch.unifr.diva.dip.api.datastructures.Point2D;
import javafx.scene.input.DataFormat;

/**
 * Data type for a list of {@code Point2D}.
 */
public class Points2D extends AbstractDataType<Point2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-point2d");

	/**
	 * Creates a new data type for a list of {@code Point2D}.
	 */
	public Points2D() {
		super(Point2D.class);
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
