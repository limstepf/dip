package ch.unifr.diva.dip.api.datatypes;

import javafx.scene.input.DataFormat;

/**
 * A data type.
 *
 * @param <T> the type of the class modeled by this data type.
 */
public interface DataType<T> {

	/**
	 * Data type collection (or cardinality) type.
	 */
	public enum CollectionType {

		/**
		 * A single value of {@code T}.
		 */
		VALUE,
		/**
		 * An array of {@code T}.
		 */
		ARRAY,
		/**
		 * A list of {@code T}.
		 */
		LIST,
		/**
		 * A set of {@code T}.
		 */
		SET,
		/**
		 * A map of {@code T}.
		 */
		MAP;
	}

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
	 * Returns the collection type of the data type.
	 *
	 * @return the collection type of the data type.
	 */
	default CollectionType getCollectionType() {
		return CollectionType.VALUE;
	}

}
