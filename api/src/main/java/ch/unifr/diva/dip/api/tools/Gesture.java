package ch.unifr.diva.dip.api.tools;

import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * A gesture. Gestures are a sets of (mouse) event handlers, used to define the
 * behaviour of tools.
 */
public interface Gesture {

	/**
	 * Returns the event handlers of the gesture.
	 *
	 * @return the event handlers.
	 */
	public List<GestureEventHandler<? extends Event>> eventHandlers();

	/**
	 * Adds an event handler to the given gesture.
	 *
	 * @param <T> type of the event.
	 * @param gesture the gesture.
	 * @param eventType the event type.
	 * @param eventHandler the event handler, or null.
	 */
	public static <T extends Event> void addEventHandler(Gesture gesture, EventType<T> eventType, EventHandler<T> eventHandler) {
		if (eventHandler != null) {
			gesture.eventHandlers().add(new GestureEventHandler<>(
					eventType,
					eventHandler
			));
		}
	}

	/**
	 * Adds (mouse) movement event handlers to the given gesture.
	 *
	 * @param gesture the gesture.
	 * @param onEntered the entered handler, or null.
	 * @param onMoved the moved handler, or null.
	 * @param onExited the exited handler, or null.
	 */
	public static void addMouseEventHandlers(Gesture gesture, EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		if (onEntered != null) {
			gesture.eventHandlers().add(new GestureEventHandler<>(
					MouseEvent.MOUSE_ENTERED,
					onEntered
			));
		}
		if (onMoved != null) {
			gesture.eventHandlers().add(new GestureEventHandler<>(
					MouseEvent.MOUSE_MOVED,
					onMoved
			));
		}
		if (onExited != null) {
			gesture.eventHandlers().add(new GestureEventHandler<>(
					MouseEvent.MOUSE_EXITED,
					onExited
			));
		}
	}

	/**
	 * Adds a key event handler to the given gesture.
	 *
	 * @param gesture the gesture.
	 * @param onKeyPressed the key event handler (triggered by
	 * {@code KeyEvent.KEY_PRESSED}), or null.
	 */
	public static void addKeyEventHandler(Gesture gesture, EventHandler<KeyEvent> onKeyPressed) {
		if (onKeyPressed != null) {
			gesture.eventHandlers().add(new GestureEventHandler<>(
					KeyEvent.KEY_PRESSED,
					onKeyPressed
			));
		}
	}

}
