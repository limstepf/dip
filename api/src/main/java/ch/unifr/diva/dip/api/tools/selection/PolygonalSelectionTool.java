package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.GestureEventHandler;
import ch.unifr.diva.dip.api.tools.PolygonGesture;
import ch.unifr.diva.dip.api.tools.brush.SymbolCrosshairCursor;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;

/**
 * Polygonal (lasso) selection tool.
 */
public class PolygonalSelectionTool extends SCCSelectionToolBase<Shape> {

	protected final PolygonGesture polyGesture;
	protected final ObservableList<Double> points;
	protected SymbolCrosshairCursor.Symbol openSymbol;
	protected int lastX;
	protected int lastY;

	/**
	 * Creates a new polygonal (lasso) selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 */
	public PolygonalSelectionTool(String name, NamedGlyph glyph) {
		this(null, null, name, glyph);
	}

	/**
	 * Creates a new polygonal (lasso) selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 */
	public PolygonalSelectionTool(EditorLayerOverlay editorOverlay, SelectionHandler<Shape> selectionHandler, String name, NamedGlyph glyph) {
		super(
				editorOverlay,
				selectionHandler,
				name,
				glyph,
				ShapeUtils.newExclusionPolyline()
		);
		this.points = ((Polyline) selection.getShape()).getPoints();
		this.polyGesture = new PolygonGesture(
				polyHandler,
				onEntered,
				onMoved,
				onExited
		);
		setGesture(polyGesture);
	}

	@Override
	protected void setZoom(double zoom) {
		super.setZoom(zoom);
		polyGesture.setZoom(zoom);
	}

	@Override
	protected Shape getMask() {
		final Polygon poly = new Polygon();
		poly.getPoints().setAll(points);
		return poly;
	}

	// set indices to last/tmp. coords.
	protected void setLast() {
		final int n = points.size();
		this.lastY = n - 1;
		this.lastX = n - 2;
	}

	protected ObservableList<Double> getPoints() {
		return points;
	}

	protected PolygonGesture getPolyGesture() {
		return polyGesture;
	}

	protected final GestureEventHandler.Handler<MouseEvent> polyHandler = (e1, e2, state) -> {
		switch (state) {
			case ANYKEY: {
				final KeyEvent e = getPolyGesture().getKeyEvent();
				setCursorSymbol(e);
				openSymbol = cursor.getSymbol();
				break;
			}
			case START: {
				final Double x = (double) Math.round(e1.getX());
				final Double y = (double) Math.round(e1.getY());
				getPoints().setAll(x, y); // start
				getPoints().addAll(x, y); // tmp. next
				setLast();
				openSymbol = cursor.getSymbol();
				getSelection().play();
				editorOverlay.getChildren().add(getSelection().getSnappedShape());
				break;
			}
			case MOVE: {
				final boolean closing = getPolyGesture().inClosingDistance(e2);
				final Double x = (double) Math.round(e2.getX());
				final Double y = (double) Math.round(e2.getY());
				getPoints().set(lastX, x); // update tmp. next
				getPoints().set(lastY, y);
				final SymbolCrosshairCursor.Symbol currentSymbol = cursor.getSymbol();
				if (closing) {
					if (!currentSymbol.equals(SymbolCrosshairCursor.Symbol.CIRCLE)) {
						openSymbol = currentSymbol;
						setCursorSymbol(SymbolCrosshairCursor.Symbol.CIRCLE);
					}
				} else {
					if (!currentSymbol.equals(openSymbol)) {
						setCursorSymbol(openSymbol);
					}
				}
				break;
			}
			case TRANSIT: {
				final Double x = (double) Math.round(e2.getX());
				final Double y = (double) Math.round(e2.getY());
				getPoints().set(lastX, x);
				getPoints().set(lastY, y);
				getPoints().addAll(x, y);
				setLast();
				break;
			}
			case END: {
				getPoints().remove(lastY); // remove tmp. point
				getPoints().remove(lastX);
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

}
