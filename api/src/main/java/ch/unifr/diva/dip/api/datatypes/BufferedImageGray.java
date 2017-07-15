package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with a gray scale color space.
 */
public class BufferedImageGray extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-gray");

	/**
	 * Creates a new data type for a {@code BufferedImage} with a gray scale
	 * color space.
	 */
	public BufferedImageGray() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
