package ch.unifr.diva.dip.api.tools;

import java.util.List;
import javafx.event.Event;

/**
 * A gesture. Gestures are a sets of (mouse) event handlers, used to define the
 * behaviour of tools.
 */
public interface Gesture {

	/**
	 * Returns the event handlers of the gesture.
	 *
	 * @param <T> type of the event.
	 * @return the event handlers.
	 */
	public <T extends Event> List<GestureEventHandler<T>> eventHandlers();

}
