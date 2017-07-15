package ch.unifr.diva.dip.api.tools;

import java.util.EventListener;
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
	public final EventHandler<T> eventHandler;

	/**
	 * Creates a new gesture event handler.
	 *
	 * @param eventType the event type.
	 * @param eventHandler the event handler.
	 */
	public GestureEventHandler(EventType<T> eventType, EventHandler<T> eventHandler) {
		this.eventType = eventType;
		this.eventHandler = eventHandler;
	}

	/**
	 * Checks whether this handler's event type is a key event.
	 *
	 * @return {@code true} if this is a key event, {@code false} otherwise.
	 */
	public boolean isKeyEvent() {
		return isKeyEvent(this.eventType);
	}

	/**
	 * Checks whether the given event type is a key event.
	 *
	 * @param t the event type.
	 * @return {@code true} if this is a key event, {@code false} otherwise.
	 */
	public static boolean isKeyEvent(EventType<?> t) {
		if (t == null) {
			return false;
		}
		// oddly enough comparing objects as in:
		// return KeyEvent.ANY.equals(t) || isKeyEvent(t.getSuperType());
		// doesn't work at all, so we compare names/strings instead:
		return ("KEY".equals(t.getName()) || isKeyEvent(t.getSuperType()));
	}

	/**
	 * Checks whether this handler's event type is a mouse event.
	 *
	 * @return {@code true} if this is a mouse event, {@code false} otherwise.
	 */
	public boolean isMouseEvent() {
		return isMouseEvent(this.eventType);
	}

	/**
	 * Checks whether the given event type is a mouse event.
	 *
	 * @param t the event type.
	 * @return {@code true} if this is a mouse event, {@code false} otherwise.
	 */
	public static boolean isMouseEvent(EventType<?> t) {
		if (t == null) {
			return false;
		}
		return ("MOUSE".equals(t.getName()) || isMouseEvent(t.getSuperType()));
	}

	/**
	 * Gesture state.
	 */
	public enum State {

		/**
		 * No particular state. But some key, not handled otherwise, just got
		 * pressed or released. Usually used to catch modifiers (shift, control,
		 * ...). Gestures operating with {@code MouseEvent} handlers should
		 * offer a method to retrieve the {@code KeyEvent}.
		 */
		ANYKEY,
		/**
		 * The abort state. Indicates that the gesture got aborted.
		 */
		ABORT,
		/**
		 * The start, or initial state. Indicates that the gesture has been
		 * started.
		 */
		START,
		/**
		 * The move state. Indicates that the gesture hasn't been finished yet.
		 * As opppsed to the transit state, nothing of importance/permanence has
		 * happended (e.g. used to indicate/visualize a possible next transit
		 * state).
		 */
		MOVE,
		/**
		 * The transit state. Indicates that the gesture hasn't been finished
		 * yet. But as opposed to the move state, something has happened (e.g. a
		 * point has been added).
		 */
		TRANSIT,
		/**
		 * The end, or final state. Indicates that the gesture has been
		 * completed.
		 */
		END;

	}

	/**
	 * Gesture event handler.
	 *
	 * @param <T> the event class this handler can handle.
	 */
	public interface Handler<T extends Event> extends EventListener {

		/**
		 * Invoked when a specific gesture for which this handler is registered
		 * happens.
		 *
		 * @param e1 the first event of the gesture.
		 * @param e2 the second event of the gesture.
		 * @param state the state of the gesture.
		 */
		void handle(T e1, T e2, State state);
	}

}
