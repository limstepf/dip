package ch.unifr.diva.dip.api.tools;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Base class for gestures.
 *
 * @param <T> type of the event.
 */
public abstract class GestureBase<T extends Event> implements Gesture {

	protected final List<GestureEventHandler<? extends Event>> eventHandlers;

	/**
	 * Creates a new gesture.
	 */
	public GestureBase() {
		this.eventHandlers = new ArrayList<>();
	}

	@Override
	public List<GestureEventHandler<? extends Event>> eventHandlers() {
		return this.eventHandlers;
	}

	/**
	 * Adds (mouse) movement event handlers.
	 *
	 * @param onEntered the entered handler, or null.
	 * @param onMoved the moved handler, or null.
	 * @param onExited the exited handler, or null.
	 */
	protected void addMouseEventHandlers(EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		Gesture.addMouseEventHandlers(this, onEntered, onMoved, onExited);
	}

}
