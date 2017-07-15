package ch.unifr.diva.dip.api.tools.selection;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.tools.MultiTool;
import ch.unifr.diva.dip.api.tools.SimpleTool;
import ch.unifr.diva.dip.api.ui.AnimatedDashedShape;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * A selection multi tool
 *
 * @param <T> class of simple tools implementing {@code SelectionTool}.
 */
public class SelectionMultiTool<T extends SimpleTool & SelectionTool<? extends Shape>> extends MultiTool {

	protected final EditorLayerOverlay editorOverlay;
	protected Shape mask;
	protected Shape previousMask;
	protected Shape bounds;
	protected AnimatedDashedShape<Shape> maskShape;
	protected final InvalidationListener maskZoomListener;
	protected final SelectionHandler<Shape> selectionHandler;
	protected final BooleanProperty hasMaskProperty;
	protected final BooleanProperty hasPreviousMaskProperty;

	/**
	 * Creates a new selection multi tool.
	 *
	 * @param editorOverlay the editor overlay.
	 * @param selectionTools the (simple) selection tools.
	 */
	@SuppressWarnings({"unchecked", "varargs"})
	public SelectionMultiTool(EditorLayerOverlay editorOverlay, T... selectionTools) {
		super(selectionTools);
		this.editorOverlay = editorOverlay;
		this.maskZoomListener = (c) -> setMaskZoom();
		this.selectionHandler = (m, isShiftDown, isControlDown) -> setMask(m, isShiftDown, isControlDown);
		this.hasMaskProperty = new SimpleBooleanProperty();
		this.hasPreviousMaskProperty = new SimpleBooleanProperty();

		for (SimpleTool tool : this.getSimpleTools()) {
			final SelectionTool<Shape> stool = (SelectionTool<Shape>) tool;
			stool.setContext(editorOverlay, selectionHandler);
		}
	}

	/**
	 * The has (selection) mask property. Is {@code true} when a selection mask
	 * is set.
	 *
	 * @return the has (selection) mask property.
	 */
	public ReadOnlyBooleanProperty hasMaskProperty() {
		return this.hasMaskProperty;
	}

	/**
	 * The has previous (selection) mask property. Is {@code true} when a
	 * previously set selection mask is stored/available.
	 *
	 * @return the has previous (selection) mask property.
	 */
	public ReadOnlyBooleanProperty hasPreviousMaskProperty() {
		return this.hasPreviousMaskProperty;
	}

	/**
	 * Updates the selection mask's zoom factor.
	 */
	protected final void setMaskZoom() {
		maskShape.setZoom(editorOverlay.getZoom());
	}

	/**
	 * Returns the (selection) mask.
	 *
	 * @return the (selection) mask.
	 */
	public Shape getMask() {
		return mask;
	}

	/**
	 * Removes the current (selection) mask.
	 */
	public void removeMask() {
		removeMaskViz();
		retainPreviousMask();
		mask = null;
		hasMaskProperty.set(false);
		onMaskRemoved();
	}

	/**
	 * Reselects the previously set (selection) mask.
	 */
	public void reselect() {
		if (previousMask == null) {
			return;
		}
		setMask(previousMask);
	}

	private void retainPreviousMask() {
		if (this.mask != null) {
			this.previousMask = this.mask;
			hasPreviousMaskProperty.set(true);
		}
	}

	private void removeMaskViz() {
		if (maskShape != null) {
			editorOverlay.zoomProperty().removeListener(maskZoomListener);
			editorOverlay.getChildren().remove(maskShape.getSnappedShape());
		}
	}

	private void putMask() {
		if (mask != null) {
			if (bounds != null) {
				mask = Shape.intersect(mask, bounds);
			}
			hasMaskProperty.set(true);
			onMaskCreated();
			maskShape = new AnimatedDashedShape<>(ShapeUtils.exclusionOutline(mask));
			setMaskZoom();
			maskShape.play();
			editorOverlay.getChildren().add(maskShape.getSnappedShape());
			editorOverlay.zoomProperty().addListener(maskZoomListener);
		} else {
			if (maskShape != null) {
				maskShape.stop();
				maskShape = null;
			}
			hasMaskProperty.set(false);
			onMaskRemoved();
		}
	}

	/**
	 * Hook method called if/after the mask has been removed. Typically used to
	 * restore/pop the stack of the graphics context (gc.restore).
	 */
	protected void onMaskRemoved() {

	}

	/**
	 * Hook method called if/after a new mask has been created. Typically used
	 * to put the mask as clipping region onto the graphics context (gc.save,
	 * draw path, gc.clip).
	 */
	protected void onMaskCreated() {

	}

	/**
	 * Handles the new selection mask.
	 *
	 * @param mask the new selection mask.
	 * @param isShiftDown whether the Shift key is pressed (addition/union).
	 * @param isControlDown whether the Control key is pressed
	 * (difference/substraction).
	 */
	protected final void setMask(Shape mask, boolean isShiftDown, boolean isControlDown) {
		if (isShiftDown) {
			addMask(mask);
		} else if (isControlDown) {
			substractMask(mask);
		} else {
			setMask(mask);
		}
	}

	/**
	 * Sets the new selection mask (as is).
	 *
	 * @param mask the new selection mask.
	 */
	public void setMask(Shape mask) {
		removeMaskViz();
		retainPreviousMask();
		this.mask = mask;
		putMask();
	}

	/**
	 * Adds the new selection mask to the current one (union).
	 *
	 * @param mask the new selection mask.
	 */
	protected void addMask(Shape mask) {
		if (this.mask == null) {
			setMask(mask);
			return;
		}
		removeMaskViz();
		retainPreviousMask();
		this.mask = Shape.union(ShapeUtils.prepareMask(this.mask), mask);
		putMask();
	}

	/**
	 * Substracts the new selection mask from the current one (difference).
	 *
	 * @param mask the new selection mask.
	 */
	protected void substractMask(Shape mask) {
		if (this.mask == null) {
			return;
		}
		removeMaskViz();
		retainPreviousMask();
		this.mask = Shape.subtract(ShapeUtils.prepareMask(this.mask), mask);
		putMask();
	}

	/**
	 * Inverts the mask (with respect to the given bounds of the working area).
	 */
	public void invertMask() {
		if (this.mask == null || this.bounds == null) {
			return;
		}
		removeMaskViz();
		retainPreviousMask();
		this.mask = Shape.subtract(this.bounds, mask);
		if (isEmptyMask(this.mask)) {
			mask = null;
			hasMaskProperty.set(false);
			onMaskRemoved();
		} else {
			putMask();
		}
	}

	private boolean isEmptyMask(Shape mask) {
		if (mask instanceof Path) {
			final Path path = (Path) mask;
			return path.getElements().isEmpty();
		}
		return false;
	}

	/**
	 * Selects all (with respect to the given bounds of the working area).
	 */
	public void selectAll() {
		if (this.bounds == null) {
			return;
		}
		removeMaskViz();
		retainPreviousMask();
		setMask(this.bounds);
	}

	/**
	 * Sets the bounds of the working area.
	 *
	 * @param bounds the bounds of the working area.
	 */
	public void setBounds(Bounds bounds) {
		setBounds(bounds.getWidth(), bounds.getHeight());
	}

	/**
	 * Sets the bounds of the working area.
	 *
	 * @param width the width of the working area.
	 * @param height the height of the working area.
	 */
	public void setBounds(double width, double height) {
		setBounds(new Rectangle(width, height));
	}

	/**
	 * Sets the bounds of the working area.
	 *
	 * @param bounds the shape of the working area.
	 */
	public void setBounds(Shape bounds) {
		this.bounds = bounds;
	}

}
