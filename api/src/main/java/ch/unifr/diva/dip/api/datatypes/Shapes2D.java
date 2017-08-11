package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code Shapes2D}.
 */
public class Shapes2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Shapes2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/shapes-2d");

	/**
	 * Creates a new data type for {@code Lines2D}.
	 */
	public Shapes2D() {
		super(ch.unifr.diva.dip.api.datastructures.Shapes2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
