package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code NestedRectangles2D}.
 */
public class NestedRectangles2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.NestedRectangles2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/nested-rectangles-2d");

	/**
	 * Creates a new data type for {@code NestedRectangles2D}.
	 */
	public NestedRectangles2D() {
		super(ch.unifr.diva.dip.api.datastructures.NestedRectangles2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
