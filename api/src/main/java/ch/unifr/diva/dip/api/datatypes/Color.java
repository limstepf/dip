
package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 *
 */
public class Color implements DataType {

	public final static Class type = javafx.scene.paint.Color.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/color");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
