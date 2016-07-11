package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A JavaFX image.
 */
public class FxImage implements DataType {

	public final static Class type = javafx.scene.image.Image.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/fx-image");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
