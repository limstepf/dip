package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.brush.SymbolCrosshairCursor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;

/**
 * Symbol crosshair cursor (SCC) selection tool base class.
 *
 * @param <T> class of the selection shape.
 */
public abstract class SCCSelectionToolBase<T extends Shape> extends SelectionToolBase<SymbolCrosshairCursor, T> {

	/**
	 * Creates a new selection tool with a symbol crosshair cursor.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param shape the shape of the selection mask (used while constructing the
	 * new selection mask; not necessarily the shape of the final mask. E.g. the
	 * polygonal selection tool doesn't use a polygon during construction, but a
	 * polyline).
	 */
	public SCCSelectionToolBase(String name, NamedGlyph glyph, T shape) {
		this(null, null, name, glyph, shape);
	}

	/**
	 * Creates a new selection tool with a symbol crosshair cursor.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param shape the shape of the selection mask (used while constructing the
	 * new selection mask; not necessarily the shape of the final mask. E.g. the
	 * polygonal selection tool doesn't use a polygon during construction, but a
	 * polyline).
	 */
	public SCCSelectionToolBase(EditorLayerOverlay editorOverlay, SelectionHandler<T> selectionHandler, String name, NamedGlyph glyph, T shape) {
		super(
				editorOverlay,
				selectionHandler,
				name,
				glyph,
				new SymbolCrosshairCursor(),
				shape
		);
	}

	protected boolean isShiftDown;
	protected boolean isControlDown;

	/**
	 * Updates the cursor symbol/modifier given a {@code KeyEvent}.
	 *
	 * @param e the key event.
	 */
	protected void setCursorSymbol(KeyEvent e) {
		setCursorSymbol(e.isShiftDown(), e.isControlDown());
	}

	/**
	 * Updates the cursor symbol/modifier given a {@code MouseEvent}.
	 *
	 * @param e the mouse event.
	 */
	protected void setCursorSymbol(MouseEvent e) {
		setCursorSymbol(e.isShiftDown(), e.isControlDown());
	}

	/**
	 * Resets the cursor symbol/modifier.
	 */
	protected void resetCursorSymbol() {
		setCursorSymbol(false, false);
	}

	/**
	 * Updates the cursor symbol/modifier.
	 *
	 * @param isShiftDown whether the Shift key is pressed (addition/union).
	 * @param isControlDown whether the Control key is pressed
	 * (difference/substraction).
	 */
	protected void setCursorSymbol(boolean isShiftDown, boolean isControlDown) {
		this.isShiftDown = false;
		this.isControlDown = false;

		if (isShiftDown) {
			this.isShiftDown = true;
			setCursorSymbol(SymbolCrosshairCursor.Symbol.PLUS);
		} else if (isControlDown) {
			this.isControlDown = true;
			setCursorSymbol(SymbolCrosshairCursor.Symbol.MINUS);
		} else {
			setCursorSymbol(SymbolCrosshairCursor.Symbol.NONE);
		}
	}

	/**
	 * Updates the cursor symbol/modifier.
	 *
	 * @param symbol the new cursor symbol/modifier.
	 */
	protected void setCursorSymbol(SymbolCrosshairCursor.Symbol symbol) {
		cursor.setSymbol(symbol);
		cursor.setZoom(editorOverlay.getZoom());
	}

	/**
	 * Returns the current cursor symbol/modifier.
	 *
	 * @return the current cursor symbol/modifier.
	 */
	protected SymbolCrosshairCursor.Symbol getCursorSymbol() {
		return cursor.getSymbol();
	}

}
