package ch.unifr.diva.dip.api.tools;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A drag gesture. Can be used for all kinds of "free hand" tools. Takes three
 * event handlers:
 *
 * <ol>
 * <li>{@code onPressed} fires once at the start of a drag gesture as the mouse
 * button is pressed.</li>
 * <li>{@code onDragged} fires repeatedly while dragging the mouse around.</li>
 * <li>{@code onReleased} fires once at the end of the drag gesture, once the
 * mouse button is released.</li>
 * </ol>
 */
public class DragGesture extends GestureBase<MouseEvent> {

	/**
	 * Creates a new drag gesture without (mouse) movement event handlers.
	 *
	 * @param onPressed the pressed handler.
	 * @param onDragged the dragged handler.
	 * @param onReleased the released handler.
	 */
	public DragGesture(EventHandler<MouseEvent> onPressed, EventHandler<MouseEvent> onDragged, EventHandler<MouseEvent> onReleased) {
		this(onPressed, onDragged, onReleased, null, null, null);
	}

	/**
	 * Creates a new drag gesture with (mouse) movement event handlers.
	 *
	 * @param onPressed the pressed handler.
	 * @param onDragged the dragged handler.
	 * @param onReleased the released handler.
	 * @param onEntered the entered handler, or null.
	 * @param onMoved the moved handler, or null.
	 * @param onExited the exited handler, or null.
	 */
	public DragGesture(EventHandler<MouseEvent> onPressed, EventHandler<MouseEvent> onDragged, EventHandler<MouseEvent> onReleased, EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		super();

		eventHandlers().add(new GestureEventHandler(
				MouseEvent.MOUSE_PRESSED,
				onPressed
		));
		eventHandlers().add(new GestureEventHandler(
				MouseEvent.MOUSE_DRAGGED,
				onDragged
		));
		eventHandlers().add(new GestureEventHandler(
				MouseEvent.MOUSE_RELEASED,
				onReleased
		));

		addMouseEventHandlers(onEntered, onMoved, onExited);
	}

}
