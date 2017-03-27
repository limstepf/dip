package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;

/**
 * A selection tool.
 */
public interface SelectionTool {

	/**
	 * Sets/updates the context of the selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 */
	public void setContext(EditorLayerOverlay editorOverlay, SelectionHandler selectionHandler);

}
