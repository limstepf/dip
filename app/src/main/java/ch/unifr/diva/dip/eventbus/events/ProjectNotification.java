package ch.unifr.diva.dip.eventbus.events;

/**
 * A project notification.
 */
public class ProjectNotification {

	/**
	 * Type of the ProjectNotification.
	 */
	public enum Type {

		/**
		 * Notification that a new project has been opened. No extra SELECTED
		 * notification is fired upon opening a project.
		 */
		OPENED,
		/**
		 * Notification that a new page has been selected.
		 */
		SELECTED,
		/**
		 * Notification that the currently selected page has been (heavily)
		 * modified. For example the whole pipeline might have been swaped, so
		 * listeners can usually interpret/treat this event as RE-SELECTED, or
		 * RE-LOADED.
		 */
		MODIFIED,
		/**
		 * Notification that a page has been removed.
		 */
		PAGE_REMOVED,
		/**
		 * Notification that a project is about to be closed. Pre-close event.
		 */
		CLOSING,
		/**
		 * Notification that a project has been closed. Post-close event.
		 */
		CLOSED
	}

	/**
	 * Type of the notification.
	 */
	public final Type type;

	/**
	 * The index of the page.
	 */
	public final int page;

	/**
	 * Creates a ProjectNotification.
	 *
	 * @param type type of the notification.
	 */
	public ProjectNotification(Type type) {
		this(type, -1);
	}

	/**
	 * Creates a ProjectNotification.
	 *
	 * @param type type of the notification.
	 * @param page index of the page.
	 */
	public ProjectNotification(Type type, int page) {
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
