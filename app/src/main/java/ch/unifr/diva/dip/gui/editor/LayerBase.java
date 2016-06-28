package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.components.EditorLayer;
import ch.unifr.diva.dip.utils.FxUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import org.slf4j.LoggerFactory;

/**
 * Layer base class. It implements EditorLayer to offer save access to OSGi
 * services (or usage from any other thread for that matter). All such methods
 * must be executed on the JavaFx application thread!
 */
public abstract class LayerBase implements Layer, EditorLayer {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(LayerBase.class);

	protected static LayerEvent MODIFIED_EVENT = new LayerEvent(LayerEvent.Type.MODIFIED);
	protected static LayerEvent MODIFIED_TREE_EVENT = new LayerEvent(LayerEvent.Type.MODIFIED_TREE);
	protected static LayerEvent DEACTIVATE_EVENT = new LayerEvent(LayerEvent.Type.DEACTIVATE);
	protected static LayerEvent REACTIVATE_PARENT_EVENT = new LayerEvent(LayerEvent.Type.REACTIVATE_PARENT);
	protected static LayerEvent REACTIVATE_EVENT = new LayerEvent(LayerEvent.Type.REACTIVATE);

	protected final TreeItem<Layer> treeItem;
	protected ObjectProperty<LayerGroup> parentProperty;
	protected final StringProperty nameProperty;
	protected final BooleanProperty visibleProperty;
	protected final ChangeListener<? super Boolean> visibleListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		this.onVisibileChanged(newValue);
	};
	protected final BooleanProperty passiveVisibleProperty;
	protected final BooleanProperty onModifiedProperty;
	protected final List<LayerExtension> layerExtensions;

	public LayerBase(String name, boolean visible, boolean passiveVisible) {
		this.treeItem = new TreeItem<>(this);
		this.parentProperty = new SimpleObjectProperty<>(null);
		this.nameProperty = new SimpleStringProperty(name);
		this.visibleProperty = new SimpleBooleanProperty(visible);
		this.visibleProperty.addListener(visibleListener);
		this.passiveVisibleProperty = new SimpleBooleanProperty(passiveVisible);
		this.onModifiedProperty = new SimpleBooleanProperty(false);
		this.layerExtensions = new ArrayList<>();
	}

	protected abstract void onVisibileChanged(boolean visible);

	protected void onRootLayerEvent(LayerEvent event) {
		switch (event.type) {
			case MODIFIED:
				this.setModifiedProperty();
				break;
		}
	}

	@Override
	public List<LayerExtension> layerExtensions() {
		return this.layerExtensions;
	}

	@Override
	public List<LayerExtension> getHiddenLayerExtensions() {
		final List<LayerGroup> layers = new ArrayList<>();
		LayerGroup parent = this.getParent();
		while (parent != null) {
			if (!parent.isHideGroup()) {
				break;
			}
			final int idx = parent.getChildren().indexOf(this);
			if (idx != 0) {
				return this.layerExtensions();
			}
			layers.add(parent);
			parent = parent.getParent();
		}
		final List<LayerExtension> extensions = new ArrayList<>();
		extensions.addAll(this.layerExtensions());
		for (int i = layers.size() - 1; i >= 0; i--) {
			extensions.addAll(layers.get(i).getHiddenLayerExtensions());
		}
		return extensions;
	}

	@Override
	public ReadOnlyBooleanProperty onModifiedProperty() {
		return this.onModifiedProperty;
	}

	protected void setModifiedProperty() {
		this.onModifiedProperty.set(!this.onModifiedProperty.get());
	}

	@Override
	public ObjectProperty<LayerGroup> parentProperty() {
		return this.parentProperty;
	}

	@Override
	public StringProperty nameProperty() {
		return this.nameProperty;
	}

	@Override
	public BooleanProperty visibleProperty() {
		return this.visibleProperty;
	}

	@Override
	public BooleanProperty passiveVisibleProperty() {
		return this.passiveVisibleProperty;
	}

	@Override
	public void setName(String name) {
		FxUtils.run(() -> {
			nameProperty().set(name);
		});
	}

	@Override
	public String getName() {
		return this.nameProperty.get();
	}

	@Override
	public String getHiddenName() {
		if (this.getParent() == null) {
			return this.getName();
		}

		if (!this.getParent().isHideGroup()) {
			return this.getName();
		}

		if (this.getName().equals("")) {
			return this.getParent().getHiddenName();
		}

		return String.format("%s: %s", this.getParent().getHiddenName(), this.getName());
	}

	@Override
	public TreeItem<Layer> getTreeItem() {
		return this.treeItem;
	}

	@Override
	public boolean isVisible() {
		try {
			return FxUtils.runFutureTask(() -> {
				return visibleProperty().get();
			});
		} catch (Exception ex) {
			log.error("failed to retrieve isVisible: {}", this, ex);
			return false;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		FxUtils.run(() -> {
			visibleProperty().set(visible);
		});
	}

	@Override
	public void remove() {
		FxUtils.run(() -> {
			if (getParent() == null) {
				return;
			}
			getParent().remove(this);
		});
	}

	@Override
	public void run(Runnable runnable) {
		FxUtils.run(runnable);
	}

	@Override
	public <T> T runFutureTask(Callable<T> callable) {
		try {
			return FxUtils.runFutureTask((Callable<T>) callable);
		} catch (Exception ex) {
			log.error("failed to runFutureTask: {}", this, ex);
			return null;
		}
	}

}
