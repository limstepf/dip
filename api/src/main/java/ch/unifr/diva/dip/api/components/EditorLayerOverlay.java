package ch.unifr.diva.dip.api.components;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * Grants access to a LayerOverlay. All methods need to be called from the
 * JavaFX application thread. The editor overlay's main purpose is the
 * visualization of tools.
 */
public interface EditorLayerOverlay {

	/**
	 * Returns the children of the overlay.
	 *
	 * @return the children of the overlay.
	 */
	public ObservableList<Node> getChildren();

	/**
	 * Adds a node to the overlay.
	 *
	 * @param node the node.
	 */
	default void add(Node node) {
		getChildren().add(node);
	}

	/**
	 * Adds all nodes to the overlay.
	 *
	 * @param nodes the nodes.
	 */
	default void addAll(Node... nodes) {
		getChildren().addAll(nodes);
	}

	/**
	 * Removes a node from the overlay.
	 *
	 * @param node the node.
	 */
	default void remove(Node node) {
		getChildren().remove(node);
	}

	/**
	 * Removes all given nodes from the overlay.
	 *
	 * @param nodes the nodes.
	 */
	default void removeAll(Node... nodes) {
		getChildren().removeAll(nodes);
	}

	/**
	 * Sets the nodes/children of the overlay.
	 *
	 * @param nodes the nodes.
	 */
	default void setAll(Node... nodes) {
		getChildren().setAll(nodes);
	}

	/**
	 * Removes all nodes/children from the overlay.
	 */
	default void clear() {
		getChildren().clear();
	}

	/**
	 * The zoom property. The read-only zoom factor of the overlay (or parent
	 * editor pane). This property is safe to bind to even across processor
	 * context switches.
	 *
	 * @return the zoom property.
	 */
	public ReadOnlyDoubleProperty zoomProperty();

	/**
	 * Returns the current zoom factor of the overlay (or parent editor pane).
	 *
	 * @return the zoom factor.
	 */
	default double getZoom() {
		return zoomProperty().get();
	}

}
