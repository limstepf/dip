package ch.unifr.diva.dip.api.components;

/**
 * Grants safe access to a LayerGroup. Unless stated otherwise all methods are
 * executed on the JavaFX application thread (i.e. safe to be called from any
 * other). Note, however, that layers attached to the JavaFX scene graph can
 * only be modified on the JavaFX application thread.
 */
public interface EditorLayerGroup extends EditorLayer {

	/**
	 * Creates a new, nameless layer group with this layer group as parent.
	 *
	 * @return a new layer group.
	 */
	public EditorLayerGroup newLayerGroup();

	/**
	 * Creates a new, named layer group with this layer group as parent.
	 *
	 * @param name name of the new layer group.
	 * @return a new layer group.
	 */
	public EditorLayerGroup newLayerGroup(String name);

	/**
	 * Creates a new, nameless layer pane with this layer group as parent.
	 *
	 * @return a new layer pane.
	 */
	public EditorLayerPane newLayerPane();

	/**
	 * Creates a new, named layer pane with this layer group as parent.
	 *
	 * @param name name of the layer pane.
	 * @return a new layer pane.
	 */
	public EditorLayerPane newLayerPane(String name);

	/**
	 * Removes a layer from this layer group.
	 *
	 * @param layer the layer to be removed from this layer group.
	 */
	public void remove(EditorLayer layer);

	/**
	 * Returns the number of children in this layer group.
	 *
	 * @return the number of children in this layer group.
	 */
	public int size();

	/**
	 * Clears this layer group.
	 */
	public void clear();

}
