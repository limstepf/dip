
package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedImage} with an YUV color space.
 */
public class BufferedImageYuv implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.imaging.BufferedMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-yuv");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
