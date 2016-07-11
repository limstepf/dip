package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A 2D polygon.
 */
public class Polygon2D implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.Polygon2D.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/polygon2d");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}
}
