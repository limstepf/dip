package ch.unifr.diva.dip.api.datatypes;

/**
 * Data type base class.
 *
 * @param <T> the type of the class modeled by this data type.
 */
public abstract class AbstractDataType<T> implements DataType<T> {

	private final Class<T> type;

	/**
	 * Creates a new data type.
	 *
	 * @param clazz the class of the data type.
	 */
	public AbstractDataType(Class<T> clazz) {
		this.type = clazz;
	}

	@Override
	public Class<T> type() {
		return type;
	}

	/*
	 * Note that the DataFormat has to be implemented by the subclass since it
	 * needs to be static (there can be only a single DataFormat object with
	 * the same identifier!).
	 */
}
