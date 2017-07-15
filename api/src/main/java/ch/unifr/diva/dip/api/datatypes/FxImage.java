package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a JavaFX image object.
 */
public class FxImage extends AbstractDataType<javafx.scene.image.Image> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/fx-image");

	/**
	 * Creates a new data type for a JavaFX image object.
	 */
	public FxImage() {
		super(javafx.scene.image.Image.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
