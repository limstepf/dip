package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a JavaFX color object. Used to encapsulate colors in the
 * default sRGB color space.
 */
public class FxColor extends AbstractDataType<javafx.scene.paint.Color> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/fx-color");

	/**
	 * Creates a new data type for a JavaFX color object.
	 */
	public FxColor() {
		super(javafx.scene.paint.Color.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
