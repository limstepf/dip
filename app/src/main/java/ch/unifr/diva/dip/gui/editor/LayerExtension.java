package ch.unifr.diva.dip.gui.editor;

import javafx.scene.Node;

/**
 * A layer extension extends the layer's TreeView by arbitrary content.
 */
public interface LayerExtension {

	/**
	 * Returns the component of the layer extension.
	 *
	 * @return the component (or view) of the layer extension.
	 */
	public Node getComponent();
}
