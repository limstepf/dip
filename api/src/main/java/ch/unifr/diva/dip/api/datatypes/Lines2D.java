package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a list of {@code Line2D}.
 */
public class Lines2D extends AbstractDataType<ch.unifr.diva.dip.api.datastructures.Lines2D> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/lines-2d");

	/**
	 * Creates a new data type for a list of {@code Line2D}.
	 */
	public Lines2D() {
		super(ch.unifr.diva.dip.api.datastructures.Lines2D.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
