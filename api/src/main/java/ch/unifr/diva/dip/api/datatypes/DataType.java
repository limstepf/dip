package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 *
 */
public interface DataType {

	Class type();

	DataFormat dataFormat();

	// true if type() is collected in a list
	default boolean isList() {
		return false;
	};
}
