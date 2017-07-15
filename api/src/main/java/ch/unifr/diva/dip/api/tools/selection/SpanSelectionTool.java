package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.GestureEventHandler;
import ch.unifr.diva.dip.api.tools.SpanGesture;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;

/**
 * Selection by spanning a shape. Base class for rectangular and elliptical
 * selection tools.
 *
 * @param <T> class of the shape.
 */
public abstract class SpanSelectionTool<T extends Shape> extends SCCSelectionToolBase<T> {

	/**
	 * Creates a new span (or spanning) selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param shape the shape of the selection mask (used while constructing the
	 * new selection mask; not necessarily the shape of the final mask. E.g. the
	 * polygonal selection tool doesn't use a polygon during construction, but a
	 * polyline).
	 */
	public SpanSelectionTool(String name, NamedGlyph glyph, T shape) {
		this(null, null, name, glyph, shape);
	}

	/**
	 * Creates a new span (or spanning) selection tool.
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
	public SpanSelectionTool(EditorLayerOverlay editorOverlay, SelectionHandler<T> selectionHandler, String name, NamedGlyph glyph, T shape) {
		super(editorOverlay, selectionHandler, name, glyph, shape);
		setGesture(new SpanGesture(
				spanHandler,
				onEntered,
				onMoved,
				onExited
		));
	}

	@SuppressWarnings("fallthrough")
	protected final GestureEventHandler.Handler<MouseEvent> spanHandler = (start, end, state) -> {
		switch (state) {
			case ANYKEY: {
				final KeyEvent e = ((SpanGesture) getGesture()).getKeyEvent();
				setCursorSymbol(e);
				break;
			}
			case START: {
				spanShape(start, end);
				getSelection().play();
				editorOverlay.getChildren().add(getSelection().getSnappedShape());
				break;
			}
			case TRANSIT: {
				onMoved.handle(end);
				spanShape(start, end);
				setCursorSymbol(end);
				break;
			}
			case END: {
				spanShape(start, end);
				selectionHandler.handle(
						getMask(),
						isShiftDown,
						isControlDown
				);
				// fall-through to ABORT
			}
			default:
			case ABORT: {
				getSelection().stop();
				editorOverlay.getChildren().remove(getSelection().getSnappedShape());
				resetCursorSymbol();
				break;
			}
		}
	};

	/**
	 * Hook method to span the shape of the new selection mask between the
	 * positions of two mouse events.
	 *
	 * @param start the first mouse event (initially set on mouse click).
	 * @param end the second mouse event (updates while dragging the mouse).
	 */
	protected abstract void spanShape(MouseEvent start, MouseEvent end);

}
