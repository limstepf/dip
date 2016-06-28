
package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 *
 */
public class Polygons2D implements DataType {

	public final static Class type = ch.unifr.diva.dip.api.datastructures.Polygon2D.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-polygon2d");

	@Override
	public Class type() {
		return type;
	}

	@Override
	public DataFormat dataFormat() {
		return dataFormat;
	}
	
	@Override
	public boolean isList() {
		return true;
	}
}
