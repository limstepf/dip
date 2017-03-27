package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

/**
 * Elliptical selection tool.
 */
public class EllipticalSelectionTool extends SpanSelectionTool<Ellipse> {

	/**
	 * Creates a new elliptical selection tool.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 */
	public EllipticalSelectionTool(String name, NamedGlyph glyph) {
		this(null, null, name, glyph);
	}

	/**
	 * Creates a new elliptical selection tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionHandler the selection handler.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 */
	public EllipticalSelectionTool(EditorLayerOverlay editorOverlay, SelectionHandler<Ellipse> selectionHandler, String name, NamedGlyph glyph) {
		super(editorOverlay,
				selectionHandler,
				name,
				glyph,
				ShapeUtils.newEllipseExclusionOutline()
		);
	}

	@Override
	protected void spanShape(MouseEvent start, MouseEvent end) {
		ShapeUtils.spanEllipse(
				selection.getShape(),
				start.getX(),
				start.getY(),
				end.getX(),
				end.getY()
		);
	}

	@Override
	protected Ellipse getMask() {
		if (selection.getShape().getRadiusX() <= 0 || selection.getShape().getRadiusY() <= 0) {
			return null;
		}
		final Ellipse mask = new Ellipse(
				selection.getShape().getCenterX(),
				selection.getShape().getCenterY(),
				selection.getShape().getRadiusX(),
				selection.getShape().getRadiusY()
		);
		mask.setFill(Color.BLACK);
		return mask;
	}

}
