package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a {@code BufferedImage} with unknown/not further specified
 * color space.
 */
public class BufferedImage extends AbstractDataType<java.awt.image.BufferedImage> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image");

	/**
	 * Creates a new data type for a {@code BufferedImage} with unknown/not
	 * further specified color space.
	 */
	public BufferedImage() {
		super(java.awt.image.BufferedImage.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
