package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with an RGBA color space.
 */
public class BufferedImageRgba extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-rgba");

	/**
	 * Creates a new data type for a {@code BufferedImage} with an RGBA color
	 * space.
	 */
	public BufferedImageRgba() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public String label() {
		return "RGBA";
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
