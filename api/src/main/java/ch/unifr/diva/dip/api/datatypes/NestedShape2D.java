package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code NestedShape2D}.
 */
public class NestedShape2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.NestedShape2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/nested-shape-2d");

	/**
	 * Creates a new data type for {@code NestedShape2D}.
	 */
	public NestedShape2D() {
		super(ch.unifr.diva.dip.api.datastructures.NestedShape2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
