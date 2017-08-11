package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code Rectangles2D}.
 */
public class Rectangles2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Rectangles2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/rectangles-2d");

	/**
	 * Creates a new data type for {@code Rectangles2D}.
	 */
	public Rectangles2D() {
		super(ch.unifr.diva.dip.api.datastructures.Rectangles2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
