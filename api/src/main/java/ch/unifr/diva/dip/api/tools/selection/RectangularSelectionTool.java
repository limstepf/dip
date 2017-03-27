package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Rectangular selection tool.
 */
public class RectangularSelectionTool extends SpanSelectionTool<Rectangle> {

	/**
	 * Creates a new rectangular selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 */
	public RectangularSelectionTool(String name, NamedGlyph glyph) {
		this(null, null, name, glyph);
	}

	/**
	 * Creates a new rectangular selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 */
	public RectangularSelectionTool(EditorLayerOverlay editorOverlay, SelectionHandler<Rectangle> selectionHandler, String name, NamedGlyph glyph) {
		super(editorOverlay,
				selectionHandler,
				name,
				glyph,
				ShapeUtils.newRectangleExclusionOutline()
		);
	}

	@Override
	protected void spanShape(MouseEvent start, MouseEvent end) {
		ShapeUtils.spanRectangle(
				selection.getShape(),
				start.getX(),
				start.getY(),
				end.getX(),
				end.getY()
		);
	}

	@Override
	protected Rectangle getMask() {
		if (selection.getShape().getWidth() <= 0 || selection.getShape().getHeight() <= 0) {
			return null;
		}
		final Rectangle mask = new Rectangle(
				selection.getShape().getX(),
				selection.getShape().getY(),
				selection.getShape().getWidth(),
				selection.getShape().getHeight()
		);
		mask.setFill(Color.BLACK);
		return mask;
	}

}
