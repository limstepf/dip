package ch.unifr.diva.dip.api.tools;

import ch.unifr.diva.dip.api.parameters.SingleRowParameter;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;

/**
 * A multi-tool (or composite tool). A multi-tool is composed of simple tools,
 * that are offered as variants to pick from. One tool is always
 * selected/active, hence a multi-tool acts as ordinary {@code Device} (or
 * simple tool).
 */
public class MultiTool implements Tool {

	protected final List<SimpleTool> tools;
	protected final ObjectProperty<SimpleTool> selectedToolProperty;
	protected final LinkedHashMap<String, SingleRowParameter> options;

	/**
	 * Creates a new multi tool.
	 *
	 * @param tools a list of simple tools.
	 */
	public MultiTool(SimpleTool... tools) {
		this(Arrays.asList(tools));
	}

	/**
	 * Creates a new multi tool.
	 *
	 * @param tools a list of simple tools.
	 */
	public MultiTool(List<SimpleTool> tools) {
		if (tools.size() < 2) {
			throw new IllegalArgumentException(String.format(
					"A multi-tool must have at least two child tools! Given: %d.",
					tools.size()
			));
		}
		this.tools = tools;
		this.selectedToolProperty = new SimpleObjectProperty(this.tools.get(0));
		this.options = new LinkedHashMap<>();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ "tools=" + this.tools
				+ ", selected=" + this.getSelectedTool()
				+ "}";
	}

	/**
	 * Returns the (simple) tools of the multi tool.
	 *
	 * @return a list of simple tools.
	 */
	public List<SimpleTool> getSimpleTools() {
		return this.tools;
	}

	/**
	 * The selected tool property.
	 *
	 * @return the selected tool property.
	 */
	public ObjectProperty<SimpleTool> selectedToolProperty() {
		return this.selectedToolProperty;
	}

	/**
	 * Selects a (simple) tool of the multi tool.
	 *
	 * @param tool a (simple) tool.
	 */
	public void setSelectTool(SimpleTool tool) {
		if (!this.tools.contains(tool)) {
			return;
		}
		this.selectedToolProperty.set(tool);
	}

	/**
	 * Returns the selected (simple) tool of the multi tool.
	 *
	 * @return the selected (simple) tool.
	 */
	public SimpleTool getSelectedTool() {
		return this.selectedToolProperty.get();
	}

	@Override
	public String getName() {
		return getSelectedTool().getName();
	}

	@Override
	public NamedGlyph getGlyph() {
		return getSelectedTool().getGlyph();
	}

	@Override
	public ObjectProperty<Cursor> cursorProperty() {
		return getSelectedTool().cursorProperty();
	}

	@Override
	public Gesture getGesture() {
		return getSelectedTool().getGesture();
	}

	@Override
	public Map<String, SingleRowParameter> options() {
		return this.options;
	}

	@Override
	public void onSelected() {
		getSelectedTool().onSelected();
	}

	@Override
	public void onDeselected() {
		getSelectedTool().onDeselected();
	}

}
