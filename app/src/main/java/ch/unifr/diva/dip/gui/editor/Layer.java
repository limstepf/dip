package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;

/**
 * Interface of (editor) layers.
 */
public interface Layer {

	/**
	 * Fires a layer event. Layer events are used to communicate from parent
	 * layers to child layers, and vice versa.
	 *
	 * @param event the layer event.
	 */
	void fireEvent(LayerEvent event);

	/**
	 * The modified property. Fires everytime some change has been made to some
	 * layer in the layer tree, but only fires on the root layer(!), otherwise
	 * the modified event just bubbles up.
	 *
	 * @return the modified property.
	 */
	ReadOnlyBooleanProperty onModifiedProperty();

	/**
	 * The parent property.
	 *
	 * @return the parent property.
	 */
	ObjectProperty<LayerGroup> parentProperty();

	/**
	 * Sets/registers the parent layer group.
	 *
	 * @param parent the parent layer group of this layer.
	 */
	default void setParent(LayerGroup parent) {
		parentProperty().set(parent);
	}

	/**
	 * Return the parent layer group.
	 *
	 * @return the parent layer group.
	 */
	default LayerGroup getParent() {
		return parentProperty().get();
	}

	/**
	 * Returns the layer's children. These are either more layers in case of a
	 * layer group, or JavaFx nodes in case of a layer pane.
	 *
	 * @return returns the layer's children.
	 */
	public ObservableList<?> getChildren();

	/**
	 * The empty property. This property is true if no content is added to the
	 * layer's canvas by this or any of it's child layers (i.e. there must be a
	 * layer pane with at least one child to be considered not empty).
	 *
	 * @return the empty property.
	 */
	ReadOnlyBooleanProperty emptyProperty();

	/**
	 * Checks whether the layer's canvas is empty, or not.
	 *
	 * @return True if the layer's canvas is empty, false otherwise.
	 */
	public boolean isEmpty();

	/**
	 * Returns a the layer extensions.
	 *
	 * @return the layer extensions.
	 */
	public List<LayerExtension> layerExtensions();

	/**
	 * Returns the "hidden" layer extensions. "Hidden" layer extensions are not
	 * necessarily the layer's own extensions. If the parent layer group is
	 * hidden, then the parent's layers are included in this list.
	 *
	 * @return the "hidden" layer extensions.
	 */
	public List<LayerExtension> getHiddenLayerExtensions();

	/**
	 * The name property of the layer.
	 *
	 * @return the name property of the layer.
	 */
	StringProperty nameProperty();

	/**
	 * Sets the name of the layer.
	 *
	 * @param name the name of the layer.
	 */
	default void setName(String name) {
		nameProperty().set(name);
	}

	/**
	 * Returns the name of the layer.
	 *
	 * @return the name of the layer.
	 */
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

	/**
	 * Sets the glyph of the layer.
	 *
	 * @param glyph the glyph of the layer.
	 */
	public void setGlyph(NamedGlyph glyph);

	/**
	 * Returns the glyph of the layer.
	 *
	 * @return the glyph of the layer, or null (for no glyph).
	 */
	public NamedGlyph getGlyph();

	/**
	 * Returns the "hidden" glyph of the layer. This is not necessarily the
	 * layer's own glyph. If the parent layer is hidden, and has a glyph, but
	 * this layer not, the the hidden parent layer's glyph is returned.
	 *
	 * @return the "hidden" glyph of the layer.
	 */
	public NamedGlyph getHiddenGlyph();

	/**
	 * The visible property.
	 *
	 * @return the visible property.
	 */
	BooleanProperty visibleProperty();

	/**
	 * Checks whether the layer is visible, or not.
	 *
	 * @return True if the layer is visible, False otherwise.
	 */
	default boolean isVisible() {
		return visibleProperty().get();
	}

	/**
	 * Toggles the visibility of the layer's conents.
	 *
	 * @param visible True to show the layer, False to not show the layer.
	 */
	default void setVisible(boolean visible) {
		visibleProperty().set(visible);
	}

	/**
	 * The passive visible property. A layer can be passively (or indirectly)
	 * invisible, meaning that the layer itself would be (directly) visible, but
	 * is effectively not due to the parent layer group being invisible (hence
	 * it's children aren't visible either).
	 *
	 * @return the passive visible property.
	 */
	BooleanProperty passiveVisibleProperty();

	/**
	 * Checks whether a layer is passively/indirectly visible.
	 *
	 * @return True if the layer is passively/indirectly visible, False
	 * otherwise.
	 */
	default boolean isPassiveVisible() {
		return passiveVisibleProperty().get();
	}

	/**
	 * Sets passive/indirect visibility of the layer.
	 *
	 * @param visible True to make the layer passively/indirectly visible, False
	 * to hide it.
	 */
	default void setPassiveVisible(boolean visible) {
		passiveVisibleProperty().set(visible);
	}

	/**
	 * Returns the component of the layer.
	 *
	 * @return the component of the layer.
	 */
	Parent getComponent();

	/**
	 * Returns the tree item of the layer. This is not necessarily the layer's
	 * own tree item, but can be a layer group's only child-tree item in case
	 * that group is hidden/reduced.
	 *
	 * @return the tree item of the layer.
	 */
	TreeItem<Layer> getTreeItem();

}
