package ch.unifr.diva.dip.eventbus.events;

/**
 * A status message event.
 */
public class StatusMessageEvent {

	/**
	 * The message.
	 */
	public final String message;

	/**
	 * Creates a new status message event with an empty message. May be used to
	 * clear the status line.
	 */
	public StatusMessageEvent() {
		this.message = "";
	}

	/**
	 * Creates a new status message event.
	 *
	 * @param message the message.
	 */
	public StatusMessageEvent(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "message=" + message
				+ "}";
	}

}
