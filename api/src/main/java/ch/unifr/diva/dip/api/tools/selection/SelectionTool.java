package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import javafx.scene.shape.Shape;

/**
 * A selection tool.
 *
 * @param <T> class of the selection shape.
 */
public interface SelectionTool<T extends Shape> {

	/**
	 * Sets/updates the context of the selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 */
	public void setContext(EditorLayerOverlay editorOverlay, SelectionHandler<T> selectionHandler);

}
