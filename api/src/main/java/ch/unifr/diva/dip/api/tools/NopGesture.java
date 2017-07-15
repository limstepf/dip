package ch.unifr.diva.dip.api.tools;

import javafx.event.Event;

/**
 * A NOP gesture. It does nothing!
 *
 * @param <T> type of the event.
 */
public class NopGesture<T extends Event> extends GestureBase {

	/**
	 * Creates a new NOP gesture.
	 */
	public NopGesture() {
		super();
	}

}
