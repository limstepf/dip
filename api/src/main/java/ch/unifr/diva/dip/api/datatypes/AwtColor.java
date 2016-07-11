package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A Java AWT color. Used to encapsulate colors in the default sRGB color space.
 */
public class AwtColor implements DataType {

	public final static Class type = java.awt.Color.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/awt-color");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
