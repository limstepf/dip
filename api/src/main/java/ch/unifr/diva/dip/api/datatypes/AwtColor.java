package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * Data type for a Java AWT color object. Used to encapsulate colors in the
 * default sRGB color space.
 */
public class AwtColor extends AbstractDataType<java.awt.Color> {

	private final static DataFormat dataFormat = new DataFormat("dip-datatype/awt-color");

	/**
	 * Creates a new data type for a Java AWT color object.
	 */
	public AwtColor() {
		super(java.awt.Color.class);
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
