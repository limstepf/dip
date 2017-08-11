package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for {@code Points2D}.
 */
public class Points2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Points2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/points-2d");

	/**
	 * Creates a new data type for {@code Points2D}.
	 */
	public Points2D() {
		super(ch.unifr.diva.dip.api.datastructures.Points2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
