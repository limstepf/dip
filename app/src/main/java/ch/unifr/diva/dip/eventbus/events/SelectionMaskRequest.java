package ch.unifr.diva.dip.eventbus.events;

/**
 * Selection mask requests.
 */
public class SelectionMaskRequest {

	/**
	 * Type of the selection mask request.
	 */
	public enum Type {

		/**
		 * Request to select the whole image document.
		 */
		ALL,
		/**
		 * Request to discard the selection mask.
		 */
		DESELECT,
		/**
		 * Request to invert the selection mask.
		 */
		INVERT,
		/**
		 * Request to reselect the previous selection mask.
		 */
		RESELECT;
	}

	/**
	 * Type of this selection mask request.
	 */
	public final Type type;

	/**
	 * Creates a new selection mask request.
	 *
	 * @param type type of the selection mask request.
	 */
	public SelectionMaskRequest(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "type=" + type.name()
				+ "}";
	}

}
