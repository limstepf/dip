package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A data type.
 *
 * @param <T> the type of the class modeled by this data type.
 */
public interface DataType<T> {

	/**
	 * The class of the data type.
	 *
	 * @return the class of the data type.
	 */
	Class<T> type();

	/**
	 * The data format of the data type.
	 *
	 * @return the data format of the data type.
	 */
	DataFormat dataFormat();

	/**
	 * The default label used for default port labels. If this method isn't
	 * overwritten then the data type's (simple) classname is used as a default
	 * label. A good default label is short and descriptive.
	 *
	 * @return the default label, or an empty string.
	 */
	default String label() {
		return "";
	}

}
