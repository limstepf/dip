package ch.unifr.diva.dip.api.tools;

import ch.unifr.diva.dip.api.utils.ShapeUtils;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * A polygon gesture.
 */
public class PolygonGesture extends GestureBase {

	protected int numPoints; // use counter instead of usual isActive boolean
	protected MouseEvent start;
	protected KeyEvent keyEvent;
	protected final double closingDistance;
	protected double invZoom;
	protected double sx;
	protected double sy;

	/**
	 * Creates a new polygon gesture.
	 *
	 * @param onGesture the gesture event handler.
	 */
	public PolygonGesture(GestureEventHandler.Handler<MouseEvent> onGesture) {
		this(onGesture, null, null, null);
	}

	/**
	 * Creates a new polygon gesture.
	 *
	 * @param onGesture the gesture event handler.
	 * @param onEntered the entered handler, or {@code null}.
	 * @param onMoved the moved (and dragged) handler, or {@code null}.
	 * @param onExited the exited handler.
	 */
	public PolygonGesture(GestureEventHandler.Handler<MouseEvent> onGesture, EventHandler<MouseEvent> onEntered, EventHandler<MouseEvent> onMoved, EventHandler<MouseEvent> onExited) {
		super();
		this.closingDistance = 16; // in pixel, no matter what zoom
		this.invZoom = 1;
		this.numPoints = 0;

		eventHandlers().add(new GestureEventHandler<>(
				MouseEvent.MOUSE_PRESSED,
				(e) -> {
					if (!e.isPrimaryButtonDown()) {
						return;
					}
					if (numPoints == 0) { // start of a new polygon
						numPoints = 1;
						setStart(e);
						onGesture.handle(e, e, GestureEventHandler.State.START);
					} else if (canClose() && (inClosingDistance(e) || isDoubleClick(e))) { // close
						numPoints = 0;
						onGesture.handle(start, e, GestureEventHandler.State.END);
					} else { // continue
						numPoints++;
						onGesture.handle(start, e, GestureEventHandler.State.TRANSIT);
					}
				}
		));

		eventHandlers().add(new GestureEventHandler<>(
				KeyEvent.KEY_PRESSED,
				(e) -> {
					keyEvent = e;
					if (e.getCode() == KeyCode.ESCAPE) {
						if (numPoints > 0) {
							numPoints = 0;
							onGesture.handle(start, start, GestureEventHandler.State.ABORT);
						}
					} else {
						onGesture.handle(start, start, GestureEventHandler.State.ANYKEY);
					}
				}
		));
		eventHandlers().add(new GestureEventHandler<>(
				KeyEvent.KEY_RELEASED,
				(e) -> {
					keyEvent = e;
					onGesture.handle(start, start, GestureEventHandler.State.ANYKEY);
				}
		));

		// merge move handlers
		if (onMoved == null) {
			final EventHandler<MouseEvent> m = (e) -> {
				if (numPoints > 0) {
					onGesture.handle(start, e, GestureEventHandler.State.MOVE);
				}
			};
			addMouseEventHandlers(onEntered, m, onExited);
		} else {
			final EventHandler<MouseEvent> m = (e) -> {
				if (numPoints > 0) {
					onGesture.handle(start, e, GestureEventHandler.State.MOVE);
				}
				onMoved.handle(e);
			};
			addMouseEventHandlers(onEntered, m, onExited);
		}
	}

	private void setStart(MouseEvent start) {
		this.start = start;
		this.sx = Math.round(start.getX());
		this.sy = Math.round(start.getY());
	}

	private boolean isDoubleClick(MouseEvent e) {
		return e.getClickCount() > 1;
	}

	/**
	 * Checks whether enough points have been registered, such that the polygon
	 * can be closed with the next (double-)click. This is the case when at
	 * least 3 points (to form a triangle) have already been registered.
	 *
	 * @return {@code true} if the polygon can be closed, {@code false}
	 * otherwise.
	 */
	public final boolean canClose() {
		return numPoints > 2;
	}

	/**
	 * Checks whether the position of the mouse event is in closing distance to
	 * the starting point of the polyline-/gon. No new point is registered if in
	 * closing distance, instead the polygon is closed.
	 *
	 * @param e the mouse event.
	 * @return {@code true} if in closing distance, {@code false} otherwise.
	 */
	public final boolean inClosingDistance(MouseEvent e) {
		return ShapeUtils.distance(sx, sy, e.getX(), e.getY()) <= (closingDistance * invZoom);
	}

	/**
	 * Sets/updates the zoom factor.
	 *
	 * @param zoom the zoom factor.
	 */
	public void setZoom(double zoom) {
		this.invZoom = 1.0 / zoom;
	}

	/**
	 * Returns the {@code KeyEvent} in case {@code ANYKEY} got fired.
	 *
	 * @return the latest {@code KeyEvent}.
	 */
	public KeyEvent getKeyEvent() {
		return this.keyEvent;
	}

}
