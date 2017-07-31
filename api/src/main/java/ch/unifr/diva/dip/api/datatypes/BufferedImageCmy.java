package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with a CMY color space.
 */
public class BufferedImageCmy extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-cmy");

	/**
	 * Creates a new data type for a {@code BufferedImage} with a CMY color
	 * space.
	 */
	public BufferedImageCmy() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public String label() {
		return "CMY";
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
