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

}
