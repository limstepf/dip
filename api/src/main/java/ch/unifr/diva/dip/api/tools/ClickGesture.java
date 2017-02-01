package ch.unifr.diva.dip.api.tools;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A click gesture. Fires already on {@code MOUSE_PRESSED}, that is, we don't
 * wait for a {@code MOUSE_RELEASED}.
 */
public class ClickGesture extends GestureBase<MouseEvent> {

	/**
	 * Creates a new click gesture.
	 *
	 * @param onClick the click handler.
	 */
	public ClickGesture(EventHandler<MouseEvent> onClick) {
		super();

		eventHandlers().add(new GestureEventHandler(
				MouseEvent.MOUSE_PRESSED,
				onClick
		));
	}

}
