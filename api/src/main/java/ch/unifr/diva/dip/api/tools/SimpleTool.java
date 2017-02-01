package ch.unifr.diva.dip.api.tools;

import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;

/**
 * A (simple) tool.
 */
public class SimpleTool implements Tool {

	protected final String name;
	protected final NamedGlyph glyph;
	protected final Gesture gesture;
	protected final LinkedHashMap<String, SingleRowParameter> options;
	protected final ObjectProperty<Cursor> cursorProperty;

	/**
	 * Creates a new simple tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the named glyph of the tool.
	 * @param gesture the gesture of the tool.
	 */
	public SimpleTool(String name, NamedGlyph glyph, Gesture gesture) {
		this(name, glyph, gesture, new SimpleObjectProperty(Cursor.CROSSHAIR));
	}

	/**
	 * Creates a new simple tool. This constructor lets you pass along a cursor
	 * property, which can be useful to create ahead in order to link it up with
	 * the passed gesture. Use the static method {@code newCursorProperty()} to
	 * create such a property.
	 *
	 * @param name the name of the tool.
	 * @param glyph the named glyph of the tool.
	 * @param gesture the gesture of the tool.
	 * @param cursorProperty the cursor property.
	 */
	public SimpleTool(String name, NamedGlyph glyph, Gesture gesture, ObjectProperty<Cursor> cursorProperty) {
		this.name = name;
		this.glyph = glyph;
		this.gesture = gesture;
		this.options = new LinkedHashMap<>();
		this.cursorProperty = cursorProperty;
	}

	/**
	 * Creates a new cursor property.
	 *
	 * @return a new cursor property.
	 */
	public static ObjectProperty<Cursor> newCursorProperty() {
		return new SimpleObjectProperty(Cursor.DEFAULT);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "name=" + this.name
				+ ", glyph=" + this.glyph
				+ ", gesture=" + this.gesture
				+ "}";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public NamedGlyph getGlyph() {
		return this.glyph;
	}

	@Override
	public ObjectProperty<Cursor> cursorProperty() {
		return this.cursorProperty;
	}

	@Override
	public Gesture getGesture() {
		return this.gesture;
	}

	@Override
	public Map<String, SingleRowParameter> options() {
		return this.options;
	}

}
