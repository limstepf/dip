package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with an YCbCr color space.
 */
public class BufferedImageYCbCr extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-ycbcr");

	/**
	 * Creates a new data type for a {@code BufferedImage} with an YCbCr color
	 * space.
	 */
	public BufferedImageYCbCr() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public String label() {
		return "YCbCr";
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
