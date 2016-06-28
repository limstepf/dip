package ch.unifr.diva.dip.eventbus.events;

/**
 * StatusMessageEvent.
 */
public class StatusMessageEvent {

	public final String message;

	public StatusMessageEvent() {
		this.message = "";
	}

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
