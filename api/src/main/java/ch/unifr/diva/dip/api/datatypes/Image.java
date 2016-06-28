
package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 *
 */
public class Image implements DataType {

	public final static Class type = javafx.scene.image.Image.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/image");
	
	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}

}
