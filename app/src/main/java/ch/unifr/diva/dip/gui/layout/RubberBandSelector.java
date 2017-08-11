package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.api.ui.AnimatedDashedShape;
import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Tool to select nodes in a master pane by rubber band selection. Holding SHIFT
 * adds to the selection, holding CTRL removes from the selection. Otherwise the
 * selection is cleared at the start of a click or a new dragging action.
 *
 * Instead of (hard) enabling/disabling this tool, it can nicely co-exist with
 * another main tool. The idea is that such a main tool indicates it's own, more
 * important action by changing the default mouse cursor to something else, then
 * the rubber band selection tool can be suppressed; or the other way around,
 * then the rubber band selection tool only works if the mouse cursor is
 * currently the default one. Care must be taken to set/change the mouse cursor
 * on the master pane, and not on some other node.
 *
 * @param <T> class to select. Must extend from Node. Make it Node to select any
 * nodes in the pane, or something more specific if needed.
 */
public class RubberBandSelector<T extends Region> {

	private final Pane master;
	private final Class<T> classFilter;
	private final BooleanProperty enabledProperty;
	private final BooleanProperty suppressIfNotDefaultCursorProperty;
	private final Rectangle rect;
	private final AnimatedDashedShape<Rectangle> animatedRect;
	private final EventHandler<MouseEvent> onMousePressed;
	private final EventHandler<MouseEvent> onMouseDragged;
	private final EventHandler<MouseEvent> onMouseReleased;
	private final ObservableSet<T> selection;
	private boolean isDragging = false;
	private boolean rectAdded = false;
	private double anchorX;
	private double anchorY;

	/**
	 * Default constructor. Selects all nodes in the master pane.
	 *
	 * @param master pane to select nodes in.
	 */
	public RubberBandSelector(Pane master) {
		this(master, Node.class);
	}

	/**
	 * Generic constructor. Selects nodes of type T in the master pane.
	 *
	 * @param master pane to select nodes in.
	 * @param classFilter Class representing T; the class to select.
	 */
	@SuppressWarnings("unchecked")
	public RubberBandSelector(Pane master, Class<? super T> classFilter) {
		this.master = master;
		this.classFilter = (Class<T>) classFilter;
		this.selection = FXCollections.observableSet();
		this.rect = ShapeUtils.newRectangleExclusionOutline();
		this.animatedRect = new AnimatedDashedShape<>(rect);

		onMousePressed = (MouseEvent e) -> {
			if (isDragging || suppressIfNotDefaultCursor(e)) {
				return;
			}

			if (!(e.isShiftDown() || e.isControlDown())) {
				selection.clear();
			}

			final Point2D p = sceneToPane(e);

			anchorX = p.getX();
			anchorY = p.getY();

			rect.setX(anchorX);
			rect.setY(anchorX);
			rect.setWidth(0);
			rect.setHeight(0);

			// don't add rect just yet to the scene since this would cause a
			// stupid bump on the ZoomPane sliders for some weird reason...
			rectAdded = false;

			isDragging = true;
			animatedRect.play();
			e.consume();
		};

		onMouseDragged = (MouseEvent e) -> {
			if (!isDragging) {
				return;
			}

			// add rect now (see above/onMousePressed)
			if (!rectAdded) {
				master.getChildren().add(rect);
				rectAdded = true;
			}

			final Point2D p = sceneToPane(e);

			// cap to positive scene-area (make this an option?)
			final double sx = (p.getX() > 0) ? p.getX() : 0.5;
			final double sy = (p.getY() > 0) ? p.getY() : 0.5;

			final double offsetX = sx - anchorX;
			final double offsetY = sy - anchorY;

			if (offsetX > 0) {
				rect.setX(anchorX);
				rect.setWidth(offsetX);
			} else {
				rect.setX(sx);
				rect.setWidth(anchorX - rect.getX());
			}

			if (offsetY > 0) {
				rect.setY(anchorY);
				rect.setHeight(offsetY);
			} else {
				rect.setY(sy);
				rect.setHeight(anchorY - rect.getY());
			}

			updateSelection(e);
			e.consume();
		};

		onMouseReleased = (MouseEvent e) -> {
			if (!isDragging) {
				return;
			}

			animatedRect.stop();
			rect.setX(0);
			rect.setY(0);
			rect.setWidth(0);
			rect.setHeight(0);

			master.getChildren().remove(rect);
			rectAdded = false;

			isDragging = false;
			e.consume();
		};

		enabledProperty = new SimpleBooleanProperty(false);
		enabledProperty.addListener((observable) -> {
			if (enabledProperty.get()) {
				addRubberBandListener(master);
			} else {
				removeRubberBandListener(master);
			}
		});

		suppressIfNotDefaultCursorProperty = new SimpleBooleanProperty(false);
	}

	private Point2D sceneToPane(MouseEvent e) {
		return master.sceneToLocal(e.getSceneX(), e.getSceneY());
	}

	private void addRubberBandListener(Pane pane) {
		pane.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressed);
		pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDragged);
		pane.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleased);
	}

	private void removeRubberBandListener(Pane pane) {
		pane.removeEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressed);
		pane.removeEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDragged);
		pane.removeEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleased);
	}

	/**
	 * Checks whether the rubber band selection should be suppressed, even if
	 * enabled. The idea is to suppress this tool in case the mouse cursor isn't
	 * the default one (indicating that some other tool is active). This allows
	 * the rubber band selection to work as a supplementing/auxiliary tool.
	 *
	 * If this isn't working as expected, make sure to set/change the mouse
	 * cursor on the correct node/region.
	 *
	 * @param e a mouse event.
	 * @return {@code true} if rubber band selection should be suppressed,
	 * {@code false} otherwise.
	 */
	private boolean suppressIfNotDefaultCursor(MouseEvent e) {
		if (!suppressIfNotDefaultCursorProperty.get()) {
			return false;
		}

		final Node src = (Node) e.getSource();
		final Cursor cursor = src.getCursor();

		if (cursor == null) {
			return false;
		}
		return !cursor.equals(Cursor.DEFAULT);
	}

	// keep in mind: this method fires over and over again while dragging!
	private void updateSelection(MouseEvent e) {
		for (Node node : master.getChildren()) {
			if (node.equals(rect)) {
				continue;
			}

			if (!classFilter.isAssignableFrom(node.getClass())) {
				continue;
			}

			@SuppressWarnings("unchecked")
			final T typedNode = (T) node;

			if (intersects(typedNode)) {
				if (e.isControlDown()) {
					selection.remove(typedNode);
				} else {
					selection.add(typedNode);
				}
			} else {
				if (!(e.isShiftDown() || e.isControlDown())) {
					selection.remove(typedNode);
				}
			}
		}
	}

	/**
	 * Intersection test ignoring padding on the target node.
	 *
	 * @param node the target node.
	 * @return {@code true} if {@code rect} intersects with the bounds of the
	 * target node (minus padding), {@code false} otherwise.
	 */
	private boolean intersects(T node) {
		final Insets padding = node.getPadding();
		final Bounds bounds = node.getBoundsInParent();
		return rect.getBoundsInParent().intersects(
				bounds.getMinX() + padding.getLeft(),
				bounds.getMinY() + padding.getTop(),
				bounds.getWidth() - padding.getLeft() - padding.getRight(),
				bounds.getHeight() - padding.getTop() - padding.getBottom()
		);
	}

	/**
	 * Enables (or disables) the rubber band selection tool.
	 *
	 * @param enable
	 */
	public void enable(boolean enable) {
		enabledProperty().set(enable);
	}

	/**
	 * Property to enable or disable to rubber band selection tool.
	 *
	 * @return the enabledProperty.
	 */
	public BooleanProperty enabledProperty() {
		return enabledProperty;
	}

	/**
	 * Suppresses the rubber band selection if the current mouse cursor is not
	 * the default one and set to {@code true}.
	 *
	 * @param enable {@code true} to suppress, {@code false} otherwise.
	 */
	public void suppressIfNotDefaultCursor(boolean enable) {
		suppressIfNotDefaultCursorProperty().set(enable);
	}

	/**
	 * Property to suppress the rubber band selection if the current mouse
	 * cursor is not the default one (indicating some ongoing action). In order
	 * to make this work, the mouse cursor needs to be changed on the correct
	 * node, that is where the mouse event will be coming from (event source);
	 * otherwise the retrieved cursor likely will be {@code null} and nothing
	 * will ever be suppressed at all.
	 *
	 * @return the suppressOnMouseCursorProperty.
	 */
	public BooleanProperty suppressIfNotDefaultCursorProperty() {
		return suppressIfNotDefaultCursorProperty;
	}

	/**
	 * The observable set of currently selected nodes.
	 *
	 * @return set of selected nodes.
	 */
	public ObservableSet<T> selection() {
		return selection;
	}

}
