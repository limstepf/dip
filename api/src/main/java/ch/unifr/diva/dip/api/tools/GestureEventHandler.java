package ch.unifr.diva.dip.api.tools;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * A gesture event handler.
 *
 * @param <T> type of the event.
 */
public class GestureEventHandler<T extends Event> {

	/**
	 * The event type.
	 */
	public final EventType<T> eventType;

	/**
	 * The event handler.
	 */
	public final EventHandler<? super T> eventHandler;

	/**
	 * Creates a new gesture event handler.
	 *
	 * @param eventType the event type.
	 * @param eventHandler the event handler.
	 */
	public GestureEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
		this.eventType = eventType;
		this.eventHandler = eventHandler;
	}

}
