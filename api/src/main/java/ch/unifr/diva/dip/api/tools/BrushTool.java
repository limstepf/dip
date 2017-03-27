package ch.unifr.diva.dip.api.tools;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.parameters.GlyphToggleGroupParameter;
import ch.unifr.diva.dip.api.parameters.IntegerParameter;
import ch.unifr.diva.dip.api.parameters.IntegerSliderParameter;
import ch.unifr.diva.dip.api.tools.brush.Brush;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.L10n;
import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;

/**
 * Brush tool base class. Usually it's already enough to implement the three
 * methods:
 *
 * <ul>
 * <li>{@code onPressed()},</li>
 * <li>{@code onDragged()}, and</li>
 * <li>{@code onReleased()}.</li>
 * </ul>
 *
 * The first one draws the initial spot, the second brushes longer strokes
 * between consecutive mouse events, and the last one is intended for cleaning
 * up.
 *
 * @param <T> type/interface of the brush.
 */
public abstract class BrushTool<T extends Brush> extends SimpleTool {

	protected final InvalidationListener zoomListener;
	protected EditorLayerOverlay editorOverlay;
	protected final List<T> brushes;
	protected T currentBrush;
	protected boolean isSelected;
	protected boolean isActive;
	protected double lastX;
	protected double lastY;

	protected final GlyphToggleGroupParameter<Integer> brushOption;
	protected final IntegerSliderParameter strokeWidthOption;
	protected final IntegerParameter strokeWidthOptionInt;

	/**
	 * Creates a new brush tool. Constructor without editor overlay, since
	 * chances are we might not be able to pass this one along already at
	 * construction time of the tool.
	 *
	 * <p>
	 * Editable processors should set the editor overlay in their
	 * {@code init(ProcessorContext context} method with a call to
	 * {@code setEditorLayerOverlay(context.overlay)}, after checking that
	 * {@code context} isn't null (which happens if called/used by the pipeline
	 * editor).
	 *
	 * Note that it is not necessary to keep updating the overlay on context
	 * switches of the processor, since the overlay is guaranteed to remain the
	 * same.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushTool(String name, NamedGlyph glyph, T... brushes) {
		this(null, name, glyph, brushes);
	}

	/**
	 * Creates a new brush tool. Constructor without editor overlay, since
	 * chances are we might not be able to pass this one along already at
	 * construction time of the tool.
	 *
	 * <p>
	 * Editable processors should set the editor overlay in their
	 * {@code init(ProcessorContext context} method with a call to
	 * {@code setEditorLayerOverlay(context.overlay)}, after checking that
	 * {@code context} isn't null (which happens if called/used by the pipeline
	 * editor).
	 *
	 * Note that it is not necessary to keep updating the overlay on context
	 * switches of the processor, since the overlay is guaranteed to remain the
	 * same.
	 *
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushTool(String name, NamedGlyph glyph, List<T> brushes) {
		this(null, name, glyph, brushes);
	}

	/**
	 * Creates a new brush tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushTool(EditorLayerOverlay editorOverlay, String name, NamedGlyph glyph, T... brushes) {
		this(editorOverlay, name, glyph, Arrays.asList(brushes));
	}

	/**
	 * Creates a new brush tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param brushes the brushes.
	 */
	public BrushTool(EditorLayerOverlay editorOverlay, String name, NamedGlyph glyph, List<T> brushes) {
		super(name, glyph);
		this.zoomListener = (c) -> updateZoom();
		setGesture(new DragGesture(
				onPressedBase, onDraggedBase, onReleasedBase,
				onEntered, onMoved, onExited
		));
		this.isActive = false;
		this.brushes = brushes;

		// setup tool options
		// TO CONSIDER: this might not scale too well with too many brushes, maybe
		// offer a dropdown menu instead of a glyph toggle (optional/dynamic).
		this.brushOption = new GlyphToggleGroupParameter("Brush", 0);
		for (int i = 0; i < this.brushes.size(); i++) {
			final T brush = this.brushes.get(i);
			brushOption.add(i, brush.getGlyph(), brush.getName());
		}
		brushOption.property().addListener((c) -> setBrush(getBrush()));

		this.strokeWidthOption = new IntegerSliderParameter(
				L10n.getInstance().getString("stroke.width"),
				1, 1, 200
		);
		strokeWidthOption.addSliderViewHook((s) -> {
			s.setMajorTickUnit(100);
			s.setMinorTickCount(4);
			s.setSnapToTicks(false);
			s.setPrefWidth(100);
		});
		this.strokeWidthOptionInt = new IntegerParameter(
				"",
				1, 1, 200
		);
		strokeWidthOptionInt.addTextFieldViewHook((t) -> {
			t.setStyle("-fx-pref-column-count: 2;");
			t.setAlignment(Pos.BASELINE_RIGHT);
		});
		strokeWidthOption.property().bindBidirectional(strokeWidthOptionInt.property());
		strokeWidthOption.property().addListener((e) -> updateZoom());

		options().put("stroke-shape", brushOption);
		options().put("stroke-width", strokeWidthOption);
		options().put("stroke-width-int", strokeWidthOptionInt);

		setBrush(this.brushes.get(0));
		setEditorLayerOverlay(editorOverlay);
	}

	/**
	 * Returns the current brush.
	 *
	 * @return the current brush.
	 */
	public final T getBrush() {
		return brushes.get(brushOption.get());
	}

	/**
	 * Returns the current stroke width.
	 *
	 * @return the stroke width.
	 */
	public final int getStrokeWidth() {
		return strokeWidthOption.get();
	}

	/**
	 * Sets the stroke width of the brush.
	 *
	 * @param size the stroke width.
	 */
	public final void setStrokeWidth(int size) {
		strokeWidthOption.set(size);
	}

	/**
	 * Sets/updates the editor layer overlay.
	 *
	 * @param editorOverlay the editor layer overlay.
	 */
	public final void setEditorLayerOverlay(EditorLayerOverlay editorOverlay) {
		this.editorOverlay = editorOverlay;
	}

	/**
	 * Checks whether an editor overlay has been registered.
	 *
	 * @return True if the editor overlay is available, False otherwise.
	 */
	protected boolean hasOverlay() {
		return editorOverlay != null;
	}

	/**
	 * Updates the brush's zoom factor (and stroke width).
	 */
	protected final void updateZoom() {
		currentBrush.setZoom(
				hasOverlay() ? editorOverlay.getZoom() : 1,
				getStrokeWidth()
		);
	}

	/**
	 * Toggles the visibility of the (custom) mouse cursor.
	 *
	 * @param visible True to show the (custom) mouse cursor, False otherwise.
	 */
	protected void setCursorVisible(boolean visible) {
		currentBrush.getCursor().setVisible(visible);
		cursorProperty().set(visible ? Cursor.NONE : Cursor.DEFAULT);
	}

	/**
	 * Moves the (custom) mouse cursor.
	 *
	 * @param e the mouse event.
	 */
	protected void moveCursor(MouseEvent e) {
		currentBrush.getCursor().setLayoutX(e.getX());
		currentBrush.getCursor().setLayoutY(e.getY());
	}

	/**
	 * Sets/changes the brush.
	 *
	 * @param brush the new brush.
	 */
	public final void setBrush(T brush) {
		if (isSelected) {
			editorOverlay.getChildren().remove(currentBrush.getCursor());
			editorOverlay.getChildren().add(brush.getCursor());
		}
		currentBrush = brush;
		updateZoom();
	}

	@Override
	public void onSelected() {
		isSelected = true;
		editorOverlay.getChildren().add(currentBrush.getCursor());
		editorOverlay.zoomProperty().addListener(zoomListener);
		updateZoom();
		cursorProperty().set(Cursor.NONE);
	}

	@Override
	public void onDeselected() {
		editorOverlay.getChildren().remove(currentBrush.getCursor());
		editorOverlay.zoomProperty().removeListener(zoomListener);
		cursorProperty().set(Cursor.DEFAULT);
		isSelected = false;
	}

	protected final EventHandler<MouseEvent> onPressedBase = (e) -> {
		if (!e.isPrimaryButtonDown()) {
			return;
		}
		isActive = true;
		onPressed(e);
		lastX = e.getX();
		lastY = e.getY();
	};

	/**
	 * The first, single brush paint.
	 *
	 * @param e the mouse event.
	 */
	protected abstract void onPressed(MouseEvent e);

	protected final EventHandler<MouseEvent> onDraggedBase = (e) -> {
		moveCursor(e);
		if (!isActive) {
			return;
		}
		onDragged(lastX, lastY, e);
		lastX = e.getX();
		lastY = e.getY();
	};

	/**
	 * A longer brush stroke while dragging the mouse.
	 *
	 * @param lastX the X-coordinate of the previous mouse event.
	 * @param lastY the Y-coordinate of the previous mouse event.
	 * @param e the current mouse event.
	 */
	protected abstract void onDragged(double lastX, double lastY, MouseEvent e);

	protected final EventHandler<MouseEvent> onReleasedBase = (e) -> {
		if (!isActive) {
			return;
		}
		onReleased(e);
		isActive = false;
	};

	/**
	 * The end of the brush stroke. Clean up. Usually nothing is painted here.
	 *
	 * @param e the mouse event.
	 */
	protected abstract void onReleased(MouseEvent e);

	protected final EventHandler<MouseEvent> onEntered = (e) -> {
		setCursorVisible(true);
	};

	protected final EventHandler<MouseEvent> onMoved = (e) -> {
		moveCursor(e);
	};

	protected final EventHandler<MouseEvent> onExited = (e) -> {
		setCursorVisible(false);
	};

}
