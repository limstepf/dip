package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.BrushTool;
import ch.unifr.diva.dip.api.tools.brush.ShapeBrush;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import java.util.Arrays;
import java.util.List;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;

/**
 * Brush selection tool
 *
 * @param <T> class of the shape brush.
 */
public class BrushSelectionTool<T extends ShapeBrush> extends BrushTool<T> implements SelectionTool {

	protected SelectionHandler selectionHandler;

	/**
	 * Creates a new brush selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushSelectionTool(String name, NamedGlyph glyph, T... brushes) {
		this(null, null, name, glyph, Arrays.asList(brushes));
	}

	/**
	 * Creates a new brush selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushSelectionTool(String name, NamedGlyph glyph, List<T> brushes) {
		this(null, null, name, glyph, brushes);
	}

	/**
	 * Creates a new brush selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushSelectionTool(EditorLayerOverlay editorOverlay, SelectionHandler<Shape> selectionHandler, String name, NamedGlyph glyph, T... brushes) {
		this(editorOverlay, selectionHandler, name, glyph, Arrays.asList(brushes));
	}

	/**
	 * Creates a new brush selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushSelectionTool(EditorLayerOverlay editorOverlay, SelectionHandler<Shape> selectionHandler, String name, NamedGlyph glyph, List<T> brushes) {
		super(editorOverlay, name, glyph, brushes);
		this.editorOverlay = editorOverlay;
		this.selectionHandler = selectionHandler;
	}

	@Override
	public void setContext(EditorLayerOverlay editorOverlay, SelectionHandler selectionHandler) {
		this.editorOverlay = editorOverlay;
		this.selectionHandler = selectionHandler;
	}

	/**
	 * Adds the shape to the (selection) mask.
	 *
	 * @param s the shape.
	 */
	protected void add(Shape s) {
		selectionHandler.handle(s, true, false);
	}

	/**
	 * Substracts the shape from the (selection) mask.
	 *
	 * @param s the shape.
	 */
	protected void substract(Shape s) {
		selectionHandler.handle(s, false, true);
	}

	@Override
	protected void onPressed(MouseEvent e) {
		final Shape s = currentBrush.paint(e);
		if (e.isControlDown()) {
			substract(s);
		} else {
			add(s);
		}
	}

	@Override
	protected void onDragged(double lastX, double lastY, MouseEvent e) {
		final Shape s = currentBrush.paintStroke(lastX, lastY, e.getX(), e.getY(), 4);
		if (e.isControlDown()) {
			substract(s);
		} else {
			add(s);
		}
	}

	@Override
	protected void onReleased(MouseEvent e) {
		// noop
	}

}
