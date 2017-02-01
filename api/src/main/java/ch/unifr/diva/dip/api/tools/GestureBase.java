package ch.unifr.diva.dip.api.tools;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;

/**
 * Base class for gestures.
 *
 * @param <T> type of the event.
 */
public abstract class GestureBase<T extends Event> implements Gesture {

	protected final List<GestureEventHandler<T>> eventHandlers;

	/**
	 * Creates a new gesture.
	 */
	public GestureBase() {
		this.eventHandlers = new ArrayList<>();
	}

	@Override
	public List<GestureEventHandler<T>> eventHandlers() {
		return this.eventHandlers;
	}

}
