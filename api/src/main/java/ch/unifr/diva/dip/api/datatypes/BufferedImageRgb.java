package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with an RGB color space.
 */
public class BufferedImageRgb extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-rgb");

	/**
	 * Creates a new data type for a {@code BufferedImage} with an RGB color
	 * space.
	 */
	public BufferedImageRgb() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
