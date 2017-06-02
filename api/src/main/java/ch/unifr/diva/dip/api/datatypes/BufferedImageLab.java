package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A {@code BufferedImage} with an LAB (or CIELAB) color space.
 */
public class BufferedImageLab implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.BufferedMatrix.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/buffered-matrix-lab");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
