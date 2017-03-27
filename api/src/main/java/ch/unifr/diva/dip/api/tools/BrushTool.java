package ch.unifr.diva.dip.api.tools;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.brush.Brush;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
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
	protected final InvalidationListener strokeWidthListener;
	protected EditorLayerOverlay editorOverlay;
	protected IntegerProperty strokeWidthProperty;
	protected T currentBrush;
	protected boolean isSelected;
	protected boolean isActive;
	protected double lastX;
	protected double lastY;

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
	 * @param brush the initial brush.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param strokeWidthProperty the stroke width property to listen to.
	 */
	public BrushTool(T brush, String name, NamedGlyph glyph, ObservableValue<? extends Number> strokeWidthProperty) {
		this(brush, name, glyph, null, strokeWidthProperty);
	}

	/**
	 * Creates a new brush tool.
	 *
	 * @param brush the initial brush.
	 * @param name the name of the tool.
	 * @param glyph the glyph of the tool.
	 * @param editorOverlay the editor overlay.
	 * @param strokeWidthProperty the stroke width property to listen to.
	 */
	public BrushTool(T brush, String name, NamedGlyph glyph, EditorLayerOverlay editorOverlay, ObservableValue<? extends Number> strokeWidthProperty) {
		super(name, glyph);
		this.zoomListener = (c) -> updateZoom();
		this.strokeWidthListener = (c) -> updateZoom();
		setGesture(new DragGesture(
				onPressedBase, onDraggedBase, onReleasedBase,
				onEntered, onMoved, onExited
		));
		this.isActive = false;
		this.strokeWidthProperty = new SimpleIntegerProperty(1);
		setBrush(brush);
		setEditorLayerOverlay(editorOverlay);
		setStrokeWidthProperty(strokeWidthProperty);
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
	 * Sets/updates the stroke width property to listen to.
	 *
	 * @param strokeWidthProperty the stroke width property.
	 */
	public final void setStrokeWidthProperty(ObservableValue<? extends Number> strokeWidthProperty) {
		this.strokeWidthProperty.bind(strokeWidthProperty);
	}

	/**
	 * Returns the current stroke width.
	 *
	 * @return the stroke width.
	 */
	protected int getStrokeWidth() {
		return strokeWidthProperty.get();
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
		strokeWidthProperty.addListener(strokeWidthListener);
		updateZoom();
		cursorProperty().set(Cursor.NONE);
	}

	@Override
	public void onDeselected() {
		editorOverlay.getChildren().remove(currentBrush.getCursor());
		editorOverlay.zoomProperty().removeListener(zoomListener);
		strokeWidthProperty.removeListener(strokeWidthListener);
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
