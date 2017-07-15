package ch.unifr.diva.dip.api.tools;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A move(-ement) gesture.
 */
public class MoveGesture extends GestureBase {

	/**
	 * Creates a new move gesture (without entered/exited handlers).
	 *
	 * @param onMoved the moved (and dragged) handler, or null.
	 */
	public MoveGesture(EventHandler<MouseEvent> onMoved) {
		this(null, onMoved, null);
	}

	/**
	 * Creates a new move gesture (with entered/exited handlers).
	 *
	 * @param onEntered the entered handler, or null.
	 * @param onMoved the moved (and dragged) handler, or null.
	 * @param onExited the exited handler.
	 */
	public MoveGesture(EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		if (onMoved != null) {
			eventHandlers().add(new GestureEventHandler<>(
					MouseEvent.MOUSE_DRAGGED,
					onMoved
			));
		}

		addMouseEventHandlers(onEntered, onMoved, onExited);
	}

}
