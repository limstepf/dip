package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code NestedRectangle2D}.
 */
public class NestedRectangle2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.NestedRectangle2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/nested-rectangle-2d");

	/**
	 * Creates a new data type for {@code NestedRectangle2D}.
	 */
	public NestedRectangle2D() {
		super(ch.unifr.diva.dip.api.datastructures.NestedRectangle2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
