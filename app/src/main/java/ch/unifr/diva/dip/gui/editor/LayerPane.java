package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.utils.FxUtils;
import java.util.Collections;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

/**
 * A layer pane (leaf). A layer pane has JavaFX nodes as children, but can be
 * considered a leaf in terms of layers.
 *
 * <p>
 * This class implements {@code EditorLayerPane} to offer save access to OSGi
 * services (or usage from any other thread for that matter). All methods of
 * this interface must be executed on the JavaFx application thread!
 */
public class LayerPane extends LayerBase implements EditorLayerPane {

	private final Pane pane;

	/**
	 * Creates a new, unnamed layer pane.
	 */
	public LayerPane() {
		this("");
	}

	/**
	 * Creates a new layer pane.
	 *
	 * @param name the name of the layer pane.
	 */
	public LayerPane(String name) {
		this(name, true, true);
	}

	/**
	 * Creates a new layer pane.
	 *
	 * @param name the name of the layer pane.
	 * @param visible direct/the layer's own visibility of the layer pane.
	 * @param passiveVisible indirect/inherited visibility of the layer pane.
	 */
	public LayerPane(String name, boolean visible, boolean passiveVisible) {
		super(name, visible, passiveVisible);

		this.pane = new Pane();
		this.pane.setBackground(Background.EMPTY);
		this.pane.getChildren().addListener(childListener);
	}

	private final InvalidationListener childListener = (c) -> {
		this.emptyProperty.set(getChildren().isEmpty());
		fireEvent(MODIFIED_EVENT);
	};

	@Override
	protected void onVisibileChanged(boolean visible) {
		this.pane.setVisible(visible);
		fireEvent(MODIFIED_EVENT);
	}

	@Override
	public void fireEvent(LayerEvent event) {
		switch (event.type) {
			case MODIFIED:
			case MODIFIED_PANE:
				handleModified(event);
				break;

			case DEACTIVATE:
				this.setPassiveVisible(false);
				break;

			case REACTIVATE_PARENT:
				if (this.getParent() != null) {
					this.getParent().fireEvent(event);
				}
				break;

			case REACTIVATE:
				this.setPassiveVisible(true);
				break;
		}
	}

	/**
	 * Returns the children of the layer pane.
	 *
	 * @return the children of the layer pane.
	 */
	@Override
	public ObservableList<Node> getChildren() {
		return this.pane.getChildren();
	}

	@Override
	public boolean isEmpty() {
		return this.pane.getChildren().isEmpty();
	}

	@Override
	public Parent getComponent() {
		return this.pane;
	}

	@Override
	public void add(Node node) {
		FxUtils.run(() -> {
			getChildren().add(node);
		});
	}

	@Override
	public void addAll(Node... nodes) {
		FxUtils.run(() -> {
			getChildren().addAll(nodes);
		});
	}

	@Override
	public void remove(Node node) {
		FxUtils.run(() -> {
			getChildren().remove(node);
		});
	}

	@Override
	public void removeAll(Node... nodes) {
		FxUtils.run(() -> {
			getChildren().removeAll(nodes);
		});
	}

	@Override
	public void clear() {
		FxUtils.run(() -> {
			getChildren().clear();
			treeItem.getChildren().clear();
		});
	}

	@Override
	public void setAll(Node... nodes) {
		FxUtils.run(() -> {
			getChildren().setAll(nodes);
		});
	}

	@Override
	public void reverseChildren() {
		FxUtils.run(() -> {
			reverse();
		});
	}

	private void reverse() {
		Collections.reverse(this.getChildren());

		fireEvent(MODIFIED_EVENT);
	}

	@Override
	public String toString() {
		final String parentName = (getParent() == null) ? "null" : getParent().getName();
		return this.getClass().getSimpleName()
				+ "{"
				+ "name=" + getName()
				+ ", parent=" + parentName
				+ ", visible=" + isVisible() + "|" + isPassiveVisible()
				+ ", children=" + pane.getChildren().size()
				+ "}";
	}

}
