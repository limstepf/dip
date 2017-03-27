package ch.unifr.diva.dip.eventbus.events;

/**
 * An application request.
 */
public class ApplicationRequest {

	/**
	 * Type of the application request.
	 */
	public enum Type {

		/**
		 * Request to open the pipeline editor.
		 */
		OPEN_PIPELINE_EDITOR,
		/**
		 * Request to open the user settings window.
		 */
		OPEN_USER_SETTINGS,
		/**
		 * Request to update the interpolation type of the main/pixel editor.
		 */
		EDITOR_INTERPOLATION,
		/**
		 * Request to exit the application.
		 */
		EXIT
	}

	/**
	 * Type of this application request.
	 */
	public final Type type;

	/**
	 * Creates an ApplicationEvent.
	 *
	 * @param type indicates the request.
	 */
	public ApplicationRequest(Type type) {
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
