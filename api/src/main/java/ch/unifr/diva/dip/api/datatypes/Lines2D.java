package ch.unifr.diva.dip.api.datatypes;

import ch.unifr.diva.dip.api.datastructures.Line2D;
import javafx.scene.input.DataFormat;

/**
 * Data type for a list of {@code Line2D}.
 */
public class Lines2D extends AbstractDataType<Line2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-line2d");

	/**
	 * Creates a new data type for a list of {@code Line2D}.
	 */
	public Lines2D() {
		super(Line2D.class);
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
