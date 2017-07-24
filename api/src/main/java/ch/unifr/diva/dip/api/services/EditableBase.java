package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.api.tools.Tool;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseEvent;

/**
 * Editable base already implements some common bits of the {@code Processor}
 * interface, offers some helper methods, and implements the {@code Editable}
 * interface.
 */
public abstract class EditableBase extends ProcessorBase implements Editable {

	protected final List<Tool> tools;
	protected final LinkedHashMap<String, SingleRowParameter<?>> options;

	/**
	 * Creates a new editable base processor.
	 *
	 * @param name name of the processor.
	 */
	public EditableBase(String name) {
		super(name);
		this.tools = new ArrayList<>();
		this.options = new LinkedHashMap<>();
	}

	protected final ObjectProperty<ProcessorContext> contextProperty = new SimpleObjectProperty<>();

	/**
	 * The context property. Editable processor typically need to manage the
	 * {@code ProcessorContext} s.t. its tools may access it at any time.
	 *
	 * <p>
	 * The context property needs to be set/updated after creating the processor
	 * in {@code newInstance()}, and after each context switch in the
	 * {@code onContextSwitch()} method.
	 *
	 * @return the current processor context.
	 */
	public ObjectProperty<ProcessorContext> contextProperty() {
		return contextProperty;
	}

	/**
	 * Sets/updates the context.
	 *
	 * @param context the latest processor context.
	 */
	public void setContext(ProcessorContext context) {
		contextProperty().set(context);
	}

	/**
	 * Returns the current context.
	 *
	 * @return the current processor context.
	 */
	public ProcessorContext getContext() {
		return contextProperty().get();
	}

	/**
	 * Checks whether the processor context has been set.
	 *
	 * @return {@code true} if the processor context has been set, {@code false}
	 * otherwise.
	 */
	public boolean hasContext() {
		return contextProperty.get() != null;
	}

	@Override
	public void onContextSwitch(ProcessorContext context, boolean saveRequired) {
		setContext(context);
	}

	@Override
	public List<Tool> tools() {
		return this.tools;
	}

	@Override
	public Map<String, SingleRowParameter<?>> options() {
		return this.options;
	}

	/**
	 * Returns the snapped x position of a mouse event.
	 *
	 * @param e the mouse event.
	 * @return the snapped x position of a mouse event.
	 */
	public static double snapX(MouseEvent e) {
		return snap(e.getX());
	}

	/**
	 * Returns the snapped y position of a mouse event.
	 *
	 * @param e the mouse event.
	 * @return the snapped y position of a mouse event.
	 */
	public static double snapY(MouseEvent e) {
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

	/**
	 * Calculates the distance between the points of two mouse events.
	 *
	 * @param e0 the first mouse event.
	 * @param e1 the second mouse event.
	 * @return the distance between the points of two mouse events.
	 */
	public static double distance(MouseEvent e0, MouseEvent e1) {
		return distance(
				e0.getX(), e0.getY(),
				e1.getX(), e1.getY()
		);
	}

	/**
	 * Calculates the distance between two points.
	 *
	 * @param x0 the x-coordinate of the first point.
	 * @param y0 the y-coordinate of the first point.
	 * @param x1 the x-coordinate of the second point.
	 * @param y1 the y-coordinate of the second point.
	 * @return the distance between the two points.
	 */
	public static double distance(double x0, double y0, double x1, double y1) {
		final double dx = x1 - x0;
		final double dy = y1 - y0;
		return Math.sqrt(dx * dx + dy * dy);
	}

}
