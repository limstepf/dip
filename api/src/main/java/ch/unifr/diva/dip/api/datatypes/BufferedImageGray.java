package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedImage} with a gray scale color space.
 */
public class BufferedImageGray implements DataType {

	public final static Class type = java.awt.image.BufferedImage.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-gray");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}
}
