package ch.unifr.diva.dip.api.tools;

import java.util.ArrayList;
import java.util.List;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Base class for gestures.
 */
public abstract class GestureBase implements Gesture {

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
	 * @param onEntered the entered handler, or {@code null}.
	 * @param onMoved the moved handler, or {@code null}.
	 * @param onExited the exited handler, or {@code null}.
	 */
	protected void addMouseEventHandlers(EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		Gesture.addMouseEventHandlers(this, onEntered, onMoved, onExited);
	}

}
