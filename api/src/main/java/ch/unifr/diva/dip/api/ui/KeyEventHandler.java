package ch.unifr.diva.dip.api.ui;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A fancy key event handler.
 */
public class KeyEventHandler implements EventHandler<KeyEvent> {

	private final KeyCode keyCode;
	private final Handler handler;

	/**
	 * Creates a new key event handler.
	 *
	 * @param keyCode the key code to listen/react to.
	 * @param handler the key event handler.
	 */
	public KeyEventHandler(KeyCode keyCode, Handler handler) {
		this.keyCode = keyCode;
		this.handler = handler;
	}

	@Override
	public void handle(KeyEvent event) {
		if (event.getCode() == keyCode) {
			if (handler.onKeyEvent(event)) {
				event.consume();
			}
		}
	}

	/**
	 * Simple, functional key event handler interface.
	 */
	public static interface Handler {

		/**
		 * Key event handler method called upon recieving a key event.
		 *
		 * @param event the key event.
		 * @return {@code true} to consume the event, {@code false} otherwise.
		 */
		public boolean onKeyEvent(KeyEvent event);
	}

}
