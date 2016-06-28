
package ch.unifr.diva.dip.api.datatypes;

import ch.unifr.diva.dip.api.datastructures.Line2D;
import javafx.scene.input.DataFormat;

/**
 *
 */
public class Lines2D implements DataType {

	public final static Class type = Line2D.class;
	public final static DataFormat dataFormat = new DataFormat("dip-datatype/list-of-line2d");

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
