package ch.unifr.diva.dip.api.tools;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * A span (or spanning) gesture. An abortable click, dragging (spanning), and
 * release gesture. Used to construct shapes by spanning them between the
 * positions of two mouse events.
 */
public class SpanGesture extends GestureBase {

	protected boolean isActive;
	protected MouseEvent start;
	protected KeyEvent keyEvent;

	/**
	 * Creates a new span gesture (without mouse movement handlers).
	 *
	 * @param onGesture the gesture event handler.
	 */
	@SuppressWarnings("rawtypes")
	public SpanGesture(GestureEventHandler.Handler onGesture) {
		this(onGesture, null, null, null);
	}

	/**
	 * Creates a new span gesture (with mouse movement handlers).
	 *
	 * @param onGesture the gesture event handler.
	 * @param onEntered the entered handler, or null.
	 * @param onMoved the moved (and dragged) handler, or null.
	 * @param onExited the exited handler.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public SpanGesture(GestureEventHandler.Handler onGesture, EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		super();

		eventHandlers().add(new GestureEventHandler<>(
				MouseEvent.MOUSE_PRESSED,
				(e) -> {
					if (isActive || !e.isPrimaryButtonDown()) {
						return;
					}
					isActive = true;
					start = e;
					onGesture.handle(start, start, GestureEventHandler.State.START);
				}
		));
		eventHandlers().add(new GestureEventHandler<>(
				MouseEvent.MOUSE_DRAGGED,
				(e) -> {
					if (!isActive) {
						return;
					}
					onGesture.handle(start, e, GestureEventHandler.State.TRANSIT);
				}
		));
		eventHandlers().add(new GestureEventHandler<>(
				MouseEvent.MOUSE_RELEASED,
				(e) -> {
					if (!isActive) {
						return;
					}
					onGesture.handle(start, e, GestureEventHandler.State.END);
					isActive = false;
				}
		));

		eventHandlers().add(new GestureEventHandler<>(
				KeyEvent.KEY_PRESSED,
				(e) -> {
					keyEvent = e;
					if (e.getCode() == KeyCode.ESCAPE) {
						if (isActive) {
							isActive = false;
							onGesture.handle(start, start, GestureEventHandler.State.ABORT);
						}
					} else {
						onGesture.handle(start, start, GestureEventHandler.State.ANYKEY);
					}
				}
		));
		eventHandlers().add(new GestureEventHandler<>(
				KeyEvent.KEY_RELEASED,
				(e) -> {
					keyEvent = e;
					onGesture.handle(start, start, GestureEventHandler.State.ANYKEY);
				}
		));

		addMouseEventHandlers(onEntered, onMoved, onExited);
	}

	/**
	 * Returns the {@code KeyEvent} in case {@code ANYKEY} got fired.
	 *
	 * @return the latest {@code KeyEvent}.
	 */
	public KeyEvent getKeyEvent() {
		return this.keyEvent;
	}

}
