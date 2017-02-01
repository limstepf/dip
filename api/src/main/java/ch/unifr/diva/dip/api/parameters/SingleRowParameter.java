package ch.unifr.diva.dip.api.parameters;

/**
 * A single row parameter. This interface marks parameters as single row
 * parameters, which makes them available for use in a bar (e.g. the options
 * bar).
 *
 * @param <T> type of the parameter.
 */
public interface SingleRowParameter<T> extends Parameter<T> {

	/**
	 * Initializes a single row parameter view. This method is called before a
	 * parameter is used as a single row parameter and intended to setup an
	 * appropriate view hook.
	 */
	public void initSingleRowView();

}
