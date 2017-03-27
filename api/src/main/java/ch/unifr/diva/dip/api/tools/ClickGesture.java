package ch.unifr.diva.dip.api.tools;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A click gesture. Fires already on {@code MOUSE_PRESSED}, that is, we don't
 * wait for a {@code MOUSE_RELEASED}.
 */
public class ClickGesture extends GestureBase<MouseEvent> {

	/**
	 * Creates a new click gesture without (mouse) movement event handlers.
	 *
	 * @param onClick the click handler.
	 */
	public ClickGesture(EventHandler<MouseEvent> onClick) {
		this(onClick, null, null, null);
	}

	/**
	 * Creates a new click gesture with (mouse) movement event handlers.
	 *
	 * @param onClick the click handler.
	 * @param onEntered the entered handler, or null.
	 * @param onMoved the moved handler, or null.
	 * @param onExited the exited handler, or null.
	 */
	public ClickGesture(EventHandler<MouseEvent> onClick, EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		super();

		eventHandlers().add(new GestureEventHandler(
				MouseEvent.MOUSE_PRESSED,
				onClick
		));

		addMouseEventHandlers(onEntered, onMoved, onExited);
	}

}
