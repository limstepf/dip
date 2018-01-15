package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.components.EditorLayer;
import ch.unifr.diva.dip.api.components.EditorLayerGroup;
import ch.unifr.diva.dip.api.components.EditorLayerPane;
import ch.unifr.diva.dip.api.utils.FxUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;

/**
 * A layer group. A layer group has other layers as children.
 *
 * <p>
 * This class implements {@code EditorLayerGroup} to offer safe access to OSGi
 * services (or usage from any other thread for that matter). All methods of
 * this interface must be executed on the JavaFx application thread!
 */
public class LayerGroup extends LayerBase implements EditorLayerGroup {

	/**
	 * Layer hide group mode. This mode only affects the Tree/TreeView.
	 */
	public enum HideGroupMode {

		/**
		 * Never hides the layer group.
		 */
		NEVER,
		/**
		 * Hides the layer group if it has exactly one child.
		 */
		AUTO,
		/**
		 * Always hides the layer group. All children's TreeItems are added
		 * directly to the layer groups's parent layer. If the layer group has
		 * no children, no TreeItem will be added/visible at all.
		 */
		ALWAYS
	}

	private final ObservableList<Layer> children = FXCollections.observableArrayList();
	private final Group group = new Group();
	private final ListChangeListener<Layer> listListener;

	/**
	 * Creates a new, unnamed and unowned layer group.
	 */
	public LayerGroup() {
		this("", -1);
	}

	/**
	 * Creates a new, unnamed layer group.
	 *
	 * @param ownerProcessorId the processor id of the processor owning this
	 * layer, or -1.
	 */
	public LayerGroup(int ownerProcessorId) {
		this("", ownerProcessorId);
	}

	/**
	 * Creates a new, unowned layer group.
	 *
	 * @param name the name of the layer group.
	 */
	public LayerGroup(String name) {
		this(name, -1);
	}

	/**
	 * Creates a new layer group.
	 *
	 * @param name the name of the layer group.
	 * @param ownerProcessorId the processor id of the processor owning this
	 * layer, or -1.
	 */
	public LayerGroup(String name, int ownerProcessorId) {
		this(name, ownerProcessorId, true, true);
	}

	/**
	 * Creates a new layer group.
	 *
	 * @param name the name of the layer group.
	 * @param ownerProcessorId the processor id of the processor owning this
	 * layer, or -1.
	 * @param visible direct/the layer's own visibility of the layer group.
	 * @param passiveVisible indirect/inherited visibility of the layer group.
	 */
	public LayerGroup(String name, int ownerProcessorId, boolean visible, boolean passiveVisible) {
		super(name, ownerProcessorId, visible, passiveVisible);

		this.listListener = (ListChangeListener.Change<? extends Layer> c) -> {
			// adhoc modification of group and tree is a bit tricky, so we just
			// manage the parent property here...
			while (c.next()) {
				if (c.wasPermutated()) {
					// permutation in range: [c.getFrom(), c.getTo()[
				} else if (c.wasUpdated()) {
					// updated all layers in range: [c.getFrom(), c.getTo()[
				} else {
					// removed layer(s)
					for (Layer layer : c.getRemoved()) {
						layer.setParent(null);
					}

					// added layer(s)
					for (Layer layer : c.getAddedSubList()) {
						layer.setParent(LayerGroup.this);
					}
				}
			}
			// ... and simply build group and tree from scratch after some modification.
			// Given tree indices are reversed and things a bit more complicated due
			// to possible hideGroup, this is probably not too bad anyways...
			setAll();
		};

		this.children.addListener(this.listListener);
	}

	private void setAll() {
		// indexing in tree is reveresed for bottom-up layers. Number of children
		// may differ due to hideGroup feature, but m isn't necessarily equal or
		// larger to n, since a hidden group that is empty doesn't add any
		// children at all (not even the empty layer group).
		//	group:  0,     1, ..., n-2, n-1
		//	tree:   m-1, m-2, ...,   1,   0

		// set group components
		final List<Parent> groups = new ArrayList<>();
		for (Layer layer : this.children) {
			groups.add(layer.getComponent());
		}
		this.group.getChildren().setAll(groups);

		// set tree items
		final List<TreeItem<Layer>> items = new ArrayList<>();

		for (int i = this.children.size() - 1; i >= 0; i--) {
			// add layers directly, or the children of a LayerGroup
			// if the group is hidden (recursively).
			final Layer layer = this.children.get(i);
			addHiddenLayer(layer, items);
		}
		this.treeItem.getChildren().setAll(items);

		this.updateHideGroup();

		// update parent
		if (getParent() != null) {
			this.updateHideGroupParent();
		}

		reevaluateEmpty();
		fireEvent(MODIFIED_EVENT);
	}

	private void addHiddenLayer(Layer layer, List<TreeItem<Layer>> items) {
		if (layer instanceof LayerGroup) {
			final LayerGroup g = (LayerGroup) layer;
			if (g.isHideGroup()) {
				// add children tree items directly, hiding the group
				for (int j = g.getChildren().size() - 1; j >= 0; j--) {
					final Layer child = g.getChildren().get(j);
					// ...and dig in deeper if necessary
					addHiddenLayer(child, items);
				}
			} else {
				items.add(g.getTreeItem());
			}
		} else {
			items.add(layer.getTreeItem());
		}
	}

	private final ObjectProperty<HideGroupMode> hideGroupModeProperty = new SimpleObjectProperty<HideGroupMode>(this, "hideGroupMode", HideGroupMode.NEVER) {
		@Override
		protected void invalidated() {
			updateHideGroup();
		}
	};

	private void updateHideGroup() {
		switch (getHideGroupMode()) {
			case AUTO:
				this.hideGroupProperty.set(getChildren().size() == 1);
				break;

			case ALWAYS:
				this.hideGroupProperty.set(true);
				break;

			default:
			case NEVER:
				this.hideGroupProperty.set(false);
				break;
		}
	}

	/**
	 * The hide group mode property.
	 *
	 * @return the hide group mode property.
	 */
	public final ObjectProperty<HideGroupMode> hideGroupModeProperty() {
		return this.hideGroupModeProperty;
	}

	/**
	 * Returns the hide group mode.
	 *
	 * @return the hide group mode.
	 */
	public final HideGroupMode getHideGroupMode() {
		return this.hideGroupModeProperty().get();
	}

	/**
	 * Sets the hide group mode.
	 *
	 * @param value the new hide group mode.
	 */
	public final void setHideGroupMode(HideGroupMode value) {
		this.hideGroupModeProperty().set(value);
	}

	private final BooleanProperty hideGroupProperty = new SimpleBooleanProperty(this, "hideGroup", false) {
		@Override
		protected void invalidated() {
			updateHideGroupParent();
		}
	};

	private void updateHideGroupParent() {
		if (this.getParent() != null) {
			this.getParent().fireEvent(MODIFIED_TREE_EVENT);
		}
	}

	/**
	 * The hide group property. Might be set/change automatically, depending on
	 * the hideGroupMode.
	 *
	 * @return the hide group property.
	 */
	public final ReadOnlyBooleanProperty hideGroupProperty() {
		return this.hideGroupProperty;
	}

	/**
	 * Checks whether the layer group is hidden or not.
	 *
	 * @return {@code true} if the layer group is hidden, {@code false}
	 * otherwise.
	 */
	public final boolean isHideGroup() {
		return hideGroupProperty.get();
	}

	/**
	 * Returns the children of the layer group.
	 *
	 * @return the children of the layer group.
	 */
	@Override
	public ObservableList<Layer> getChildren() {
		return this.children;
	}

	@Override
	public boolean isEmpty() {
		return this.emptyProperty().get();
	}

	private void reevaluateEmpty() {
		this.emptyProperty.set(checkEmpty());
	}

	private boolean checkEmpty() {
		for (Layer layer : getChildren()) {
			if (!layer.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onVisibileChanged(boolean visible) {
		this.group.setVisible(visible);
		if (visible) {
			fireEvent(REACTIVATE_EVENT);
		} else {
			fireEvents(DEACTIVATE_EVENT);
		}
		fireEvent(MODIFIED_EVENT);
	}

	@Override
	public void fireEvent(LayerEvent event) {
		switch (event.type) {
			case MODIFIED:
			case MODIFIED_PANE:
				handleModified(event);
				break;

			case MODIFIED_TREE:
				setAll();
				break;

			case MODIFIED_EMPTY:
				reevaluateEmpty();
				break;

			case DEACTIVATE:
				this.setPassiveVisible(false);
				fireEvents(DEACTIVATE_EVENT);
				break;

			case REACTIVATE_PARENT:
				if (this.getParent() != null) {
					if (!this.getParent().isPassiveVisible()) {
						this.getParent().fireEvent(event);
					} else {
						this.setVisible(true);
					}
				}
				break;

			case REACTIVATE:
				this.setPassiveVisible(true);
				if (this.isVisible()) {
					fireEvents(event);
				}
				break;
		}
	}

	// bubble down
	private void fireEvents(LayerEvent event) {
		for (Layer layer : this.children) {
			layer.fireEvent(event);
		}
	}

	@Override
	public Parent getComponent() {
		return this.group;
	}

	@Override
	public EditorLayerGroup newLayerGroup() {
		return newLayerGroup("");
	}

	@Override
	public EditorLayerGroup newLayerGroup(final String name) {
		if (Platform.isFxApplicationThread()) {
			return createLayerGroup(name);
		}

		try {
			return FxUtils.runFutureTask(() -> {
				return createLayerGroup(name);
			});
		} catch (Exception ex) {
			log.error("failed to create a new LayerGroup: {}", this, ex);
			return null;
		}
	}

	private LayerGroup createLayerGroup(final String name) {
		final LayerGroup layer = new LayerGroup(name);
		LayerGroup.this.getChildren().add(layer);
		return layer;
	}

	@Override
	public EditorLayerPane newLayerPane() {
		return newLayerPane("");
	}

	@Override
	public EditorLayerPane newLayerPane(final String name) {
		if (Platform.isFxApplicationThread()) {
			return createLayerPane(name);
		}

		try {
			return FxUtils.runFutureTask(() -> {
				return createLayerPane(name);
			});
		} catch (Exception ex) {
			if (ex instanceof InterruptedException) {
				return null;
			}
			log.error("failed to create a new LayerPane: {}", this, ex);
			return null;
		}
	}

	private LayerPane createLayerPane(final String name) {
		final LayerPane layer = new LayerPane(name);
		LayerGroup.this.getChildren().add(layer);
		return layer;
	}

	@Override
	public void reverseChildren() {
		FxUtils.run(() -> {
			reverse();
		});
	}

	private void reverse() {
		this.children.removeListener(this.listListener);
		Collections.reverse(this.children);
		this.children.addListener(this.listListener);

		setAll();
	}

	@Override
	public void remove(EditorLayer layer) {
		FxUtils.run(() -> {
			getChildren().remove((Layer) layer);
		});
	}

	@Override
	public void clear() {
		FxUtils.run(() -> {
			getChildren().clear();
		});
	}

	@Override
	public String toString() {
		final String parentName;
		final int siblings;
		if (getParent() == null) {
			parentName = "<null>";
			siblings = 0;
		} else {
			parentName = getParent().getName();
			siblings = getParent().getChildren().size() - 1;
		}
		return this.getClass().getSimpleName()
				+ "{"
				+ "name=" + getName()
				+ ", parent=" + parentName
				+ ", siblings=" + siblings
				+ ", visible=" + isVisible() + "|" + isPassiveVisible()
				+ ", hidden=" + isHideGroup()
				+ ", children=" + getChildren().size()
				+ ", empty=" + isEmpty()
				+ "}";
	}

}
