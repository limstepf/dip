package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.SimpleTool;
import ch.unifr.diva.dip.api.tools.brush.Cursor;
import ch.unifr.diva.dip.api.ui.AnimatedDashedShape;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;

/**
 * Selection tool base class.
 *
 * @param <C> class of the node that implements {@code Cursor}.
 * @param <T> class of the selection shape.
 */
public abstract class SelectionToolBase<C extends Node & Cursor, T extends Shape> extends SimpleTool implements SelectionTool<T> {

	protected EditorLayerOverlay editorOverlay;
	protected SelectionHandler<T> selectionHandler;
	protected final C cursor;
	protected final AnimatedDashedShape<T> selection;
	protected final InvalidationListener zoomListener = (c) -> setZoom();

	/**
	 * Creates a new selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param cursor the cursor of the tool.
	 * @param shape the shape of the selection mask (used while constructing the
	 * new selection mask; not necessarily the shape of the final mask. E.g. the
	 * polygonal selection tool doesn't use a polygon during construction, but a
	 * polyline).
	 */
	public SelectionToolBase(String name, NamedGlyph glyph, C cursor, T shape) {
		this(null, null, name, glyph, cursor, shape);
	}

	/**
	 * Creates a new selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param cursor the cursor of the tool.
	 * @param shape the shape of the selection mask (used while constructing the
	 * new selection mask; not necessarily the shape of the final mask. E.g. the
	 * polygonal selection tool doesn't use a polygon during construction, but a
	 * polyline).
	 */
	public SelectionToolBase(EditorLayerOverlay editorOverlay, SelectionHandler<T> selectionHandler, String name, NamedGlyph glyph, C cursor, T shape) {
		super(name, glyph);
		this.editorOverlay = editorOverlay;
		this.selectionHandler = selectionHandler;
		this.cursor = cursor;
		this.selection = new AnimatedDashedShape<>(shape);
	}

	@Override
	public void setContext(EditorLayerOverlay editorOverlay, SelectionHandler<T> selectionHandler) {
		this.editorOverlay = editorOverlay;
		this.selectionHandler = selectionHandler;
	}

	/**
	 * Checks whether an editor overlay has been registered.
	 *
	 * @return {@code true} if the editor overlay is available, {@code false}
	 * otherwise.
	 */
	protected boolean hasOverlay() {
		return editorOverlay != null;
	}

	private void setZoom() {
		final double zoom = hasOverlay() ? editorOverlay.getZoom() : 1;
		setZoom(zoom);
	}

	/**
	 * Sets/updates the zoom factor.
	 *
	 * @param zoom the new zoom factor.
	 */
	protected void setZoom(double zoom) {
		selection.setZoom(zoom);
		cursor.setZoom(zoom);
	}

	/**
	 * Returns the selection mask (used for construction).
	 *
	 * @return the selection mask (used for construction).
	 */
	protected AnimatedDashedShape<T> getSelection() {
		return selection;
	}

	/**
	 * Returns the final/constructed selection mask.
	 *
	 * @return the final/constructed selection mask.
	 */
	protected abstract T getMask();

	// need to be passed to the gesture, onMoved should be called also onDragged
	protected final EventHandler<MouseEvent> onEntered = (e) -> setCursorVisible(true);
	protected final EventHandler<MouseEvent> onMoved = (e) -> moveCursor(e);
	protected final EventHandler<MouseEvent> onExited = (e) -> setCursorVisible(false);

	/**
	 * Toggles the visibility of the (custom) mouse cursor.
	 *
	 * @param visible {@code true} to show the (custom) mouse cursor,
	 * {@code false} otherwise.
	 */
	protected final void setCursorVisible(boolean visible) {
		cursor.setVisible(visible);
		cursorProperty().set(visible ? javafx.scene.Cursor.NONE : javafx.scene.Cursor.DEFAULT);
	}

	/**
	 * Moves the (custom) mouse cursor.
	 *
	 * @param e the mouse event.
	 */
	protected final void moveCursor(MouseEvent e) {
		cursor.setLayoutX(e.getX());
		cursor.setLayoutY(e.getY());
	}

	@Override
	public void onSelected() {
		editorOverlay.getChildren().add(cursor);
		cursorProperty().set(javafx.scene.Cursor.NONE);
		editorOverlay.zoomProperty().addListener(zoomListener);
		setZoom();
	}

	@Override
	public void onDeselected() {
		editorOverlay.getChildren().remove(cursor);
		cursorProperty().set(javafx.scene.Cursor.DEFAULT);
		editorOverlay.zoomProperty().removeListener(zoomListener);
	}

}
