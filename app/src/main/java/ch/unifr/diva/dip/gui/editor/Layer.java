package ch.unifr.diva.dip.gui.editor;

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

	public String getHiddenName();

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
