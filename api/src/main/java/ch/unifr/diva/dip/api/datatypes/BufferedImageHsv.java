package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedImage} with a HSV/HSB color space.
 */
public class BufferedImageHsv implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.imaging.BufferedMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-hsv");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}
}
