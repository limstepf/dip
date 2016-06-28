package ch.unifr.diva.dip.api.components;

import javafx.scene.Node;

/**
 * Grants safe access to a LayerPane. All methods are executed on the JavaFX
 * application thread.
 */
public interface EditorLayerPane extends EditorLayer {

	/**
	 * Adds a node to the layer pane.
	 *
	 * @param node node to be added to the layer pane.
	 */
	public void add(Node node);

	/**
	 * Adds nodes to the layer pane.
	 *
	 * @param nodes nodes to be added to the layer pane.
	 */
	public void addAll(Node... nodes);

	/**
	 * Removes a node from the layer pane.
	 *
	 * @param node node to be removed from the layer pane.
	 */
	public void remove(Node node);

	/**
	 * Removes nodes from the layer pane.
	 *
	 * @param nodes nodes to be removed from the layer pane.
	 */
	public void removeAll(Node... nodes);

	/**
	 * Sets all nodes of the layer pane.
	 *
	 * @param nodes nodes to be set as children of the layer pane.
	 */
	public void setAll(Node... nodes);

	/**
	 * Clears the layer pane.
	 */
	public void clear();
}
