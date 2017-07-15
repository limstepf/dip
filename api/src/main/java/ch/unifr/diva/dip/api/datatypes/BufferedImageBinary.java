package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with a binary color space.
 */
public class BufferedImageBinary extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-binary");

	/**
	 * Creates a new data type for a {@code BufferedImage} with a binary color
	 * space.
	 */
	public BufferedImageBinary() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
