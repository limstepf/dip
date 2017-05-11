package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.components.EditorLayer;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.api.utils.FxUtils;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
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
import javafx.scene.paint.Color;
import org.slf4j.LoggerFactory;

/**
 * Layer base class.
 *
 * <p>
 * This class implements {@code EditorLayer} to offer save access to OSGi
 * services (or usage from any other thread for that matter). All methods of
 * this interface must be executed on the JavaFx application thread!
 */
public abstract class LayerBase implements Layer, EditorLayer {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(LayerBase.class);

	protected static LayerEvent MODIFIED_EVENT = new LayerEvent(LayerEvent.Type.MODIFIED);
	protected static LayerEvent MODIFIED_PANE_EVENT = new LayerEvent(LayerEvent.Type.MODIFIED_PANE);
	protected static LayerEvent MODIFIED_TREE_EVENT = new LayerEvent(LayerEvent.Type.MODIFIED_TREE);
	protected static LayerEvent MODIFIED_EMPTY_EVENT = new LayerEvent(LayerEvent.Type.MODIFIED_EMPTY);
	protected static LayerEvent DEACTIVATE_EVENT = new LayerEvent(LayerEvent.Type.DEACTIVATE);
	protected static LayerEvent REACTIVATE_PARENT_EVENT = new LayerEvent(LayerEvent.Type.REACTIVATE_PARENT);
	protected static LayerEvent REACTIVATE_EVENT = new LayerEvent(LayerEvent.Type.REACTIVATE);

	protected final TreeItem<Layer> treeItem;
	protected final int ownerProcessorId;
	protected ObjectProperty<LayerGroup> parentProperty;
	protected final StringProperty nameProperty;
	protected NamedGlyph glyph;
	protected final BooleanProperty visibleProperty;
	protected final ChangeListener<? super Boolean> visibleListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
		this.onVisibileChanged(newValue);
	};
	protected final BooleanProperty passiveVisibleProperty;
	protected final List<LayerExtension> layerExtensions;

	/**
	 * Creates a new layer.
	 *
	 * @param name the name of the layer.
	 * @param visible direct/the layer's own visibility of the layer.
	 * @param passiveVisible indirect/inherited visibility of the layer.
	 */
	public LayerBase(String name, boolean visible, boolean passiveVisible) {
		this(name, -1, visible, passiveVisible);
	}

	/**
	 * Creates a new layer.
	 *
	 * @param name the name of the layer.
	 * @param ownerProcessorId the processor id of the processor owning this
	 * layer, or -1.
	 * @param visible direct/the layer's own visibility of the layer.
	 * @param passiveVisible indirect/inherited visibility of the layer.
	 */
	public LayerBase(String name, int ownerProcessorId, boolean visible, boolean passiveVisible) {
		this.treeItem = new TreeItem<>(this);
		this.parentProperty = new SimpleObjectProperty<>(null);
		this.nameProperty = new SimpleStringProperty(name);
		this.ownerProcessorId = ownerProcessorId;
		this.visibleProperty = new SimpleBooleanProperty(visible);
		this.visibleProperty.addListener(visibleListener);
		this.passiveVisibleProperty = new SimpleBooleanProperty(passiveVisible);
		this.layerExtensions = new ArrayList<>();
	}

	protected final BooleanProperty emptyProperty = new SimpleBooleanProperty(this, "empty", true) {
		@Override
		protected void invalidated() {
			final LayerGroup parent = parentProperty().get();
			if (parent != null) {
				parent.fireEvent(MODIFIED_EMPTY_EVENT);
			}
		}
	};

	@Override
	public ReadOnlyBooleanProperty emptyProperty() {
		return this.emptyProperty;
	}

	protected abstract void onVisibileChanged(boolean visible);

	protected void handleModified(LayerEvent event) {
		// modified events bubble up to the root layer
		if (this.getParent() == null) {
			switch (event.type) {
				case MODIFIED:
					this.setModifiedProperty();
					break;
				case MODIFIED_PANE:
					this.setModifiedProperty();
					this.setModifiedContentProperty();
					break;
			}
		} else {
			this.getParent().fireEvent(event);
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

	/*
	 * modified property fires for changes to the layer-/structure itself
	 * (e.g. toggling visibility), and for manual repaints alike.
	 */
	protected BooleanProperty onModifiedProperty;

	@Override
	public BooleanProperty onModifiedProperty() {
		if (this.onModifiedProperty == null) {
			this.onModifiedProperty = new SimpleBooleanProperty(false);
		}
		return this.onModifiedProperty;
	}

	protected void setModifiedProperty() {
		this.onModifiedProperty().set(!this.onModifiedProperty().get());
	}

	/*
	 * modified content property fires for manually requested repaints
	 * only in order to mark the project as dirty.
	 */
	protected BooleanProperty onModifiedContentProperty;

	@Override
	public BooleanProperty onModifiedContentProperty() {
		if (this.onModifiedContentProperty == null) {
			this.onModifiedContentProperty = new SimpleBooleanProperty(false);
		}
		return this.onModifiedContentProperty;
	}

	protected void setModifiedContentProperty() {
		this.onModifiedContentProperty().set(!this.onModifiedContentProperty().get());
	}

	@Override
	public void repaint() {
		fireEvent(MODIFIED_PANE_EVENT);
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
	public void setGlyph(NamedGlyph glyph) {
		this.glyph = glyph;
	}

	@Override
	public NamedGlyph getGlyph() {
		return this.glyph;
	}

	private ObjectProperty<Color> glyphColorProperty; // lazily initialized

	public ObjectProperty<Color> glyphColorProperty() {
		if (glyphColorProperty == null) {
			glyphColorProperty = new SimpleObjectProperty<>(UIStrategyGUI.Colors.processing);
		}
		return glyphColorProperty;
	}

	@Override
	public void setGlyphColor(Color color) {
		if (this.glyph == null) {
			return;
		}
		glyphColorProperty().set(color);
	}

	@Override
	public Color getGlyphColor() {
		if (glyphColorProperty == null) {
			return UIStrategyGUI.Colors.processing;
		}
		return glyphColorProperty().get();
	}

	@Override
	public ObjectProperty<Color> getHiddenGlyphColorProperty() {
		if (this.getParent() == null) {
			return glyphColorProperty();
		}

		if (!this.getParent().isHideGroup()) {
			return glyphColorProperty();
		}

		if (this.glyph == null) {
			return this.getParent().glyphColorProperty();
		}

		return glyphColorProperty();
	}

	@Override
	public NamedGlyph getHiddenGlyph() {
		if (this.getParent() == null) {
			return this.glyph;
		}

		if (!this.getParent().isHideGroup()) {
			return this.glyph;
		}

		if (this.glyph == null) {
			return this.getParent().getHiddenGlyph();
		}

		return this.glyph;
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
	public int getOwnerProcessorId() {
		// check for direct ownership first
		if (this.ownerProcessorId >= 0) {
			return this.ownerProcessorId;
		}
		// check of some parent layer is owned by some processor
		final LayerGroup parent = getParent();
		if (parent != null) {
			return parent.getOwnerProcessorId();
		}
		// not owned by a processor
		return -1;
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
