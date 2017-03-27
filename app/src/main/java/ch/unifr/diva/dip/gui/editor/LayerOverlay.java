package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.gui.layout.Zoomable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * A layer overlay. The overlay sits on top of all other layers and is always
 * bilinearly interpolated while upscaling. Its primary usage is intended for
 * custom cursors, selection masks, and similar things.
 */
public class LayerOverlay implements EditorLayerOverlay {

	protected final Pane overlay;

	/**
	 * Creates a new overlay.
	 */
	public LayerOverlay() {
		this.overlay = new Pane();
	}

	/**
	 * Returns the node of the overlay.
	 *
	 * @return the node.
	 */
	public Node getNode() {
		return overlay;
	}

	/**
	 * Zoom property.
	 */
	protected final DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

	/**
	 * Sets the zoomable of the overlay to bind to.
	 *
	 * @param zoomable the zoomable.
	 */
	public void setZoomable(Zoomable zoomable) {
		zoomProperty.bind(zoomable.zoomProperty());
	}

	@Override
	public ReadOnlyDoubleProperty zoomProperty() {
		return zoomProperty;
	}

	@Override
	public ObservableList<Node> getChildren() {
		return overlay.getChildren();
	}

}
