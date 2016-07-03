package ch.unifr.diva.dip.eventbus.events;

/**
 * An application request.
 */
public class ApplicationRequest {

	/**
	 * Type of the ApplicationRequest.
	 */
	public enum Type {

		/**
		 * Request to show the sidebar.
		 */
		SHOW_SIDEBAR,
		/**
		 * Request to hide the sidebar.
		 */
		HIDE_SIDEBAR,
		/**
		 * Request to open the pipeline editor.
		 */
		OPEN_PIPELINE_EDITOR,
		/**
		 * Request to open the user settings window.
		 */
		OPEN_USER_SETTINGS,
		/**
		 * Request to exit the application.
		 */
		EXIT
	}
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
