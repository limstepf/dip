package ch.unifr.diva.dip.api.ui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * A fancy mouse event handler.
 */
public class MouseEventHandler implements EventHandler<MouseEvent> {

	/**
	 * Mouse click count.
	 */
	public enum ClickCount {

		/**
		 * A single click.
		 */
		SINGLE_CLICK(1),
		/**
		 * A double click.
		 */
		DOUBLE_CLICK(2);

		private final int clickCount;

		ClickCount(int clickCount) {
			this.clickCount = clickCount;
		}

		/**
		 * Returns the number of required clicks.
		 *
		 * @return the number of required clicks.
		 */
		public int clicks() {
			return clickCount;
		}
	}

	private final ClickCount clickCount;
	private final Handler handler;

	/**
	 * Creates a new mouse event handler.
	 *
	 * @param clickCount required click count to activate.
	 * @param handler the mouse event handler.
	 */
	public MouseEventHandler(ClickCount clickCount, Handler handler) {
		this.clickCount = clickCount;
		this.handler = handler;
	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getClickCount() >= clickCount.clicks()) {
			if (handler.onMouseEvent(event)) {
				event.consume();
			}
		}
	}

	/**
	 * Simple, functional mouse event handler interface.
	 */
	public static interface Handler {

		/**
		 * Handler method called upon recieving a mouse event.
		 *
		 * @param event the mouse event.
		 * @return True to consume the event, False otherwise.
		 */
		public boolean onMouseEvent(MouseEvent event);
	}

}
