package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A JavaFX color. Used to encapsulate colors in the default sRGB color space.
 */
public class FxColor implements DataType {

	public final static Class type = javafx.scene.paint.Color.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/fx-color");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
