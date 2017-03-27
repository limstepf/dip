package ch.unifr.diva.dip.api.tools.selection;

import javafx.scene.shape.Shape;

/**
 * A selection mask handler.
 *
 * @param <T> class of the shape of the selection mask.
 */
public interface SelectionHandler<T extends Shape> {

	/**
	 * Handles the new selection mask.
	 *
	 * @param mask the new selection mask.
	 * @param isShiftDown whether the Shift key is pressed (addition/union).
	 * @param isControlDown whether the Control key is pressed
	 * (difference/substraction).
	 */
	public void handle(T mask, boolean isShiftDown, boolean isControlDown);

}
