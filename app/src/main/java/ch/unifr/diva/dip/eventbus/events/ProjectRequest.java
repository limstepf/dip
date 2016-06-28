package ch.unifr.diva.dip.eventbus.events;

/**
 * A project request.
 */
public class ProjectRequest {

	/**
	 * Type of the ProjectRequest.
	 */
	public enum Type {

		/**
		 * Request to create a new project.
		 */
		NEW,
		/**
		 * Request to open an existing project.
		 */
		OPEN,
		/**
		 * Request to repair an invalid/corrupt project, and open it if succeeded.
		 */
		REPAIR,
		/**
		 * Request to select a new page of the project.
		 */
		SELECT,
		/**
		 * Request to import new project pages.
		 */
		IMPORT_PAGES,
		/**
		 * Request to save the current project.
		 */
		SAVE,
		/**
		 * Request to save the project at a new location.
		 */
		SAVE_AS,
		/**
		 * Request to close the current project.
		 */
		CLOSE
	}
	public final Type type;
	public final int page;

	/**
	 * Creates a ProjectRequest.
	 *
	 * @param type indicates the request.
	 */
	public ProjectRequest(Type type) {
		this(type, -1);
	}

	/**
	 * Creates a ProjectRequest.
	 *
	 * @param type indicates the request.
	 * @param page index of the page.
	 */
	public ProjectRequest(Type type, int page) {
		this.type = type;
		this.page = page;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "type=" + type.name()
				+ ", page=" + page
				+ "}";
	}
}
