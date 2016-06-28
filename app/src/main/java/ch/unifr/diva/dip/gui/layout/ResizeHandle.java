
package ch.unifr.diva.dip.gui.layout;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * A ResizeHandle resizes a (parent) region it binds to.
 */
public class ResizeHandle extends BorderPane {

	private final static double HANDLE_MARGIN = 5;
	private Region parent;
	private final Region handle = new Region();
	private boolean initMinHeight;
	private boolean dragging;
	private double y;

	public ResizeHandle(Region parent) {
		super();

		handle.getStyleClass().add("dip-resize-handle");
		handle.setMaxWidth(Double.MAX_VALUE);
		handle.setMinHeight(HANDLE_MARGIN);
		handle.setMaxHeight(HANDLE_MARGIN);

		this.setPadding(new Insets(0, 0, HANDLE_MARGIN * -1, 0));
		this.setCenter(handle);

		bind(parent);
	}

	private void bind(Region parent) {
		this.parent = parent;
		initMinHeight = false;

		handle.setOnMouseEntered(e -> onMouseEntered(e));
		handle.setOnMouseExited(e -> onMouseExited(e));
		handle.setOnMousePressed(e -> onMousePressed(e));
		handle.setOnMouseReleased(e -> onMouseReleased(e));
		handle.setOnMouseDragged(e -> onMouseDragged(e));
	}

	private void onMouseEntered(MouseEvent e) {
		this.setCursor(Cursor.S_RESIZE);
	}

	private void onMouseExited(MouseEvent e) {
		if (!dragging) {
			this.setCursor(Cursor.DEFAULT);
		}
	}

	private void onMousePressed(MouseEvent e) {
		dragging = true;
		y = e.getSceneY();
		if (!initMinHeight) {
			parent.setMinHeight(parent.getHeight());
			initMinHeight = true;
		}
		this.setCursor(Cursor.S_RESIZE);
	}

	private void onMouseReleased(MouseEvent e) {
		dragging = false;
		this.setCursor(Cursor.DEFAULT);
	}

	private void onMouseDragged(MouseEvent e) {
		if (!dragging) {
			return;
		}
		double y2 = e.getSceneY();
		double h = parent.getMinHeight() + (y2 - y);
		parent.setMinHeight(h);
		y = y2;
	}
}
