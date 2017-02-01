package ch.unifr.diva.dip.api.tools;

import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;

/**
 * A tool. A simple tool is composed of a name, a glyph (used in the toolbar),
 * and a gesture, which is a set of event handlers that define the behaviour of
 * the tool. Furthermore there's a cursor property to adjust the mouse cursor,
 * and a map of options (exclusively) associated to the tool (the processor
 * offering the tool may itself have a map of shared options).
 *
 * <p>
 * Highly related, simple tools (e.g. variants/methods to do the basically same
 * thing, like different selection tools) can be lumped together in a
 * multi-tool. Multi-tools are just such a set of simple tools, which always
 * have one of the simple tools selected as current (or active) tool. A
 * different simple tool can be selected from the toolbar by keeping the primary
 * mouse button pressed for a moment.
 *
 * <p>
 * This follows the composite pattern with the notable difference that a
 * multi-tool (the composite) can not have just any tool as a child, but only
 * simple tools. I.e. you can't put a multi-tool into another multi-tool.
 */
public interface Tool {

	/**
	 * Returns the name of the tool.
	 *
	 * @return the name of the tool.
	 */
	public String getName();

	/**
	 * Returns the glyph of the tool.
	 *
	 * @return the glyph of the tool, or null (for no special glyph).
	 */
	default NamedGlyph getGlyph() {
		return null;
	}

	/**
	 * Returns the cursor property. This property will be bound to the cursor
	 * property of the editor's pane while a tool is active, so the tool is free
	 * to change the mouse cursor at will.
	 *
	 * <p>
	 * The best place to do so is most likely an event handler of the gesture of
	 * the tool. To make this work, there is a constructor for
	 * {@code SimpleTool} that takes a cursor property to be defined ahead (with
	 * {@code SimpleTool.newCursorProperty()}), s.t. you can refer to it while
	 * defining the gesture of the tool.
	 *
	 * @return the cursor property.
	 */
	public ObjectProperty<Cursor> cursorProperty();

	/**
	 * Returns the gesture of the tool. A gesture is composed of a set of event
	 * handlers.
	 *
	 * @return the gesture of the tool.
	 */
	public Gesture getGesture();

	/**
	 * The options of the tool.
	 *
	 * @return the options of the tool.
	 */
	public Map<String, SingleRowParameter> options();

	/**
	 * Checks whether this tool has options.
	 *
	 * @return True if this tool has at least one option, False otherwise.
	 */
	default boolean hasOptions() {
		return !options().isEmpty();
	}

	/**
	 * Checks whether this is a multi-tool.
	 *
	 * @return True if this is a multi-tool, False otherwise.
	 */
	default boolean isMultiTool() {
		return (this instanceof MultiTool);
	}

	/**
	 * Returns this tool as a multi-tool. Make sure this tool
	 * {@code isMultiTool()} first.
	 *
	 * @return a multi-tool.
	 */
	default MultiTool asMultiTool() {
		return (MultiTool) this;
	}

}
