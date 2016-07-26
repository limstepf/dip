package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;

/**
 * Interface of (editor) layers.
 */
public interface Layer {

	void fireEvent(LayerEvent event);

	// only fires on root layer!
	ReadOnlyBooleanProperty onModifiedProperty();

	ObjectProperty<LayerGroup> parentProperty();

	default void setParent(LayerGroup parent) {
		parentProperty().set(parent);
	}

	default LayerGroup getParent() {
		return parentProperty().get();
	}

	public List<LayerExtension> layerExtensions();

	public List<LayerExtension> getHiddenLayerExtensions();

	StringProperty nameProperty();

	default void setName(String name) {
		nameProperty().set(name);
	}

	default String getName() {
		return nameProperty().get();
	}

	/**
	 * Returns the "hidden" name of the layer. The "hidden" name of a layer is
	 * not necessarily the layer's own name. If the parent layer group is
	 * hidden, then the name is either the parents name (if this layer's name is
	 * the empty string), or the parents name get's prepended to this layer's
	 * name (if not the empty string).
	 *
	 * @return the "hidden" name. Should be use to display the layer's name
	 * instead of directly calling {@code getName()}.
	 */
	public String getHiddenName();

	public void setGlyph(NamedGlyph glyph);

	/**
	 * Returns the glyph of the layer.
	 *
	 * @return the glyph of the layer, or null (for no glyph).
	 */
	public NamedGlyph getGlyph();

	public NamedGlyph getHiddenGlyph();

	BooleanProperty visibleProperty();

	default boolean isVisible() {
		return visibleProperty().get();
	}

	default void setVisible(boolean visible) {
		visibleProperty().set(visible);
	}

	BooleanProperty passiveVisibleProperty();

	default boolean isPassiveVisible() {
		return passiveVisibleProperty().get();
	}

	default void setPassiveVisible(boolean visible) {
		passiveVisibleProperty().set(visible);
	}

	Parent getComponent();

	// possibly reduced to a child-treeItem
	TreeItem<Layer> getTreeItem();

}
