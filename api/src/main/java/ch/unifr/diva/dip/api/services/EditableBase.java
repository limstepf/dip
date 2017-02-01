package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.api.tools.Tool;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.input.MouseEvent;

/**
 * EditableBase already implements some common bits of the {@code Processor}
 * interface, offers some helper methods, and implements the {@code Editable}
 * interface.
 */
public abstract class EditableBase extends ProcessorBase implements Editable {

	protected final List<Tool> tools;
	protected final LinkedHashMap<String, SingleRowParameter> options;

	/**
	 * Creates the base of a new editable processor.
	 *
	 * @param name name of the processor.
	 */
	public EditableBase(String name) {
		super(name);
		this.tools = new ArrayList<>();
		this.options = new LinkedHashMap<>();
	}

	@Override
	public List<Tool> tools() {
		return this.tools;
	}

	@Override
	public Map<String, SingleRowParameter> options() {
		return this.options;
	}

	/**
	 * Returns the snapped x position of a mouse event.
	 *
	 * @param e the mouse event.
	 * @return the snapped x position of a mouse event.
	 */
	public static double x(MouseEvent e) {
		return snap(e.getX());
	}

	/**
	 * Returns the snapped y position of a mouse event.
	 *
	 * @param e the mouse event.
	 * @return the snapped y position of a mouse event.
	 */
	public static double y(MouseEvent e) {
		return snap(e.getY());
	}

	/**
	 * Snaps a x or y coordinate to the middle of a pixel. The JavaFX coordinate
	 * system works in double precision. To draw/get sharp strokes, coordinates
	 * need to be snapped ({@code floor(v) + 0.5}) to the middle of a pixel.
	 *
	 * @param value the x or y coordinate.
	 * @return the coordinated snapped to the middle of a pixel.
	 */
	public static double snap(double value) {
		return (int) value + 0.5;
	}

}
