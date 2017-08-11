package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code NestedShapes2D}.
 */
public class NestedShapes2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.NestedShapes2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/nested-shapes-2d");

	/**
	 * Creates a new data type for {@code NestedShapes2D}.
	 */
	public NestedShapes2D() {
		super(ch.unifr.diva.dip.api.datastructures.NestedShapes2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
