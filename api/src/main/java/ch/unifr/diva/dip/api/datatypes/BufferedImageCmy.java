
package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedImage} with a CMY color space.
 */
public class BufferedImageCmy implements DataType {

	public final static Class type = java.awt.image.BufferedImage.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-image-cmy");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}
}
