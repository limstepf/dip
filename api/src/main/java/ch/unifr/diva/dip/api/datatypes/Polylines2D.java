package ch.unifr.diva.dip.api.datatypes;

import ch.unifr.diva.dip.api.datastructures.Polyline2D;
import javafx.scene.input.DataFormat;

/**
 * A list of {@code Polylines2D}.
 */
public class Polylines2D implements DataType {

	public final static Class type = Polyline2D.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-polyline2d");

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
