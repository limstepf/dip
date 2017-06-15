package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.KeyEventHandler;
import ch.unifr.diva.dip.api.ui.MouseEventHandler;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.layout.DraggableListCell;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

/**
 * A list view to display data items. The list view can be either selectable
 * (used to import) or editable (used to export data items). The constructor is
 * declared protected; use the provided factory methods instead.
 *
 * @param <T> class of the data items.
 */
public class DataItemListView<T extends DataItemListView.DataItem> implements Localizable {

	protected final ListView<T> listView = new ListView<>();
	protected final ContextMenu contextMenu = new ContextMenu();
	protected boolean isEditable;
	protected final BooleanProperty hasSelectionProperty = new SimpleBooleanProperty(false);
	protected final BooleanProperty hasOneSelectedProperty = new SimpleBooleanProperty(false);
	protected final InvalidationListener selectionListener = (obs) -> {
		hasSelectionProperty.set(
				!listView.getSelectionModel().isEmpty()
		);
		hasOneSelectedProperty.set(
				listView.getSelectionModel().getSelectedItems().size() == 1
		);
	};

	/**
	 * Creates a new, selectable data item list view.
	 *
	 * @param <T> class of the data items.
	 * @param selectionCallback callback function called once an item has been
	 * picked (by double-click).
	 * @return a new, selectable data item list view.
	 */
	public static <T extends DataItemListView.DataItem> DataItemListView<T> newSelectableDataItemListView(Callback<T, Void> selectionCallback) {
		return new DataItemListView(false, selectionCallback);
	}

	/**
	 * Creates a new, editable data item list view. Use the methods
	 * {@code addMenuItem} and {@code getDeleteItemMenuItem} to attach deletion
	 * functionality to the context menu.
	 *
	 * @param <T> class of the data items.
	 * @return a new, editable data item list view.
	 */
	public static <T extends DataItemListView.DataItem> DataItemListView<T> newEditableDataItemListView() {
		return new DataItemListView(true, null);
	}

	/**
	 * Creates a new data item list view.
	 *
	 * @param editable whether the items are edtibale, or selectable.
	 * @param selectionCallback the selection callback in case the items are
	 * selectable.
	 */
	protected DataItemListView(boolean editable, Callback<T, Void> selectionCallback) {
		this.isEditable = editable;
		if (editable) {
			listView.setEditable(true);
			listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			listView.setCellFactory((ListView<T> param) -> new DataItemListView.EditableDataItemListCell());
		} else {
			listView.setEditable(false);
			listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			listView.setCellFactory((ListView<T> param) -> new DataItemListView.SelectableDataItemListCell(selectionCallback));
		}
		listView.setMaxHeight(Double.MAX_VALUE);
		listView.getSelectionModel().getSelectedItems().addListener(selectionListener);
		listView.setContextMenu(contextMenu);
	}

	/**
	 * Checks whether the list view is editable or selectable.
	 *
	 * @return true if the list view is editable, false otherwise.
	 */
	public boolean isEditable() {
		return this.isEditable;
	}

	/**
	 * Changes the editability of the list. This can be useful in order to have
	 * a list with draggable/rearrangable items that still aren't editable.
	 *
	 * @param editable True to set the list to be editable, False otherwise.
	 */
	public void setEditable(boolean editable) {
		this.isEditable = editable;
		listView.setEditable(editable);
	}

	/**
	 * Returns the context menu of the list view.
	 *
	 * @return the context menu of the list view.
	 */
	public ContextMenu getContextMenu() {
		return this.contextMenu;
	}

	/**
	 * Adds a menu item to the context menu.
	 *
	 * @param item the menu item.
	 */
	public void addMenuItem(MenuItem item) {
		this.contextMenu.getItems().add(item);
	}

	/**
	 * Creates a delete item(s) menu item.
	 *
	 * @return the menu item.
	 */
	public MenuItem getDeleteItemMenuItem() {
		return getDeleteItemMenuItem(localize("item.any"));
	}

	/**
	 * Creates a delete item(s) menu item. The returned menu item still needs to
	 * be added manually to the context menu!
	 *
	 * @param label the label of an item. Should be lowercase and catch the
	 * singular and plural case (e.g. "item(s)").
	 * @return the menu item.
	 */
	public MenuItem getDeleteItemMenuItem(String label) {
		return getDeleteItemMenuItem(label, (e) -> {
			getItems().removeAll(getSelectedItems());
		});
	}

	/**
	 * Creates a delete item(s) menu item with a custom event handler. The
	 * returned menu item still needs to be added manually to the context menu!
	 *
	 * @param label the label of an item. Should be lowercase and catch the
	 * singular and plural case (e.g. "item(s)").
	 * @param handler a custom event handler. Needs to take care of the removal
	 * of currently selected items.
	 * @return the menu item.
	 */
	public MenuItem getDeleteItemMenuItem(String label, EventHandler<ActionEvent> handler) {
		final MenuItem item = new MenuItem(localize("delete.selected", label.toLowerCase()));
		item.disableProperty().bind(Bindings.not(hasSelectionProperty));
		item.setOnAction(handler);
		return item;
	}

	/**
	 * Returns the has selection property. This property is true if at least one
	 * item in the list is selected.
	 *
	 * @return the has selection property.
	 */
	public BooleanProperty hasSelectionProperty() {
		return this.hasSelectionProperty;
	}

	/**
	 * Returns the has one selected (item) property. This property is true if
	 * exactly one item in the list is selected.
	 *
	 * @return the has one selected (item) property.
	 */
	public BooleanProperty hasOneSelectedProperty() {
		return this.hasOneSelectedProperty;
	}

	/**
	 * Sets the selection mode of the list view.
	 *
	 * @param mode the selection mode.
	 */
	public void setSelectionMode(SelectionMode mode) {
		listView.getSelectionModel().setSelectionMode(mode);
	}

	/**
	 * Returns the (root-) node of the view.
	 *
	 * @return the node of the view.
	 */
	public ListView<T> getNode() {
		return listView;
	}

	/**
	 * Returns the items of the list.
	 *
	 * @return returns the items of the list.
	 */
	final public ObservableList<T> getItems() {
		return listView.getItems();
	}

	/**
	 * Returns the selected index.
	 *
	 * @return returns the selected index.
	 */
	final public int getSelectedIndex() {
		return listView.getSelectionModel().getSelectedIndex();
	}

	/**
	 * Returns the selected item.
	 *
	 * @return the selected item.
	 */
	final public T getSelectedItem() {
		return listView.getSelectionModel().getSelectedItem();
	}

	/**
	 * Returns the selected items.
	 *
	 * @return the selected items.
	 */
	final public List<T> getSelectedItems() {
		return new ArrayList<>(this.listView.getSelectionModel().getSelectedItems());
	}

	/**
	 * Sets the items (or underlying observable list) of the list view.
	 *
	 * @param items the observable list of items.
	 */
	public void setItems(ObservableList<T> items) {
		this.listView.setItems(items);
		this.listView.layout();
	}

	/**
	 * Selectable data item list cell.
	 *
	 * @param <T> class of the data items.
	 */
	protected static class SelectableDataItemListCell<T extends DataItemListView.DataItem> extends ListCell<T> implements Localizable {

		protected final Label presetLabel;
		protected final Callback<T, Void> selectionCallback;
		protected T currentItem;
		protected final EventHandler<MouseEvent> onDoubleClickHandler;

		/**
		 * Creates a new, selectable data item list cell.
		 *
		 * @param selectionCallback callback called upon selecting an item, or
		 * null.
		 */
		public SelectableDataItemListCell(Callback<T, Void> selectionCallback) {
			this.selectionCallback = selectionCallback;
			this.onDoubleClickHandler = new MouseEventHandler(
					MouseEventHandler.ClickCount.DOUBLE_CLICK,
					(e) -> onSelected(currentItem)
			);
			this.presetLabel = new Label();
		}

		private boolean onSelected(T item) {
			if (selectionCallback != null) {
				selectionCallback.call(currentItem);
			}
			return true;
		}

		@Override
		public final void updateItem(T item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			presetLabel.removeEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);

			if (!empty && item != null) {
				currentItem = item;
				presetLabel.setText(item.nameProperty().get());
				presetLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);
				setGraphic(presetLabel);
			} else {
				currentItem = null;
			}
		}

	}

	/**
	 * Editable data item list cell.
	 *
	 * @param <T> class of the data items.
	 */
	protected static class EditableDataItemListCell<T extends DataItemListView.DataItem> extends DraggableListCell<T> implements Localizable {

		protected final BorderPane pane;
		protected final Label dtLabel;
		protected final Label dtDescription;
		protected final TextField dtTextField;
		protected T currentItem;
		protected final EventHandler<KeyEvent> onEnterHandler = new KeyEventHandler(
				KeyCode.ENTER,
				(e) -> {
					cancelEdit();
					return true;
				}
		);
		protected final EventHandler<MouseEvent> onDoubleClickHandler = new MouseEventHandler(
				MouseEventHandler.ClickCount.DOUBLE_CLICK,
				(e) -> {
					cancelEdit();
					return true;
				}
		);
		protected NamedGlyph currentNamedGlyph;
		protected Glyph currentGlyph;

		/**
		 * Creates a new, editable data item list cell.
		 */
		public EditableDataItemListCell() {
			this.pane = new BorderPane();
			this.dtLabel = new Label();
			dtLabel.setMaxWidth(Double.MAX_VALUE);
			this.dtDescription = new Label();
			dtDescription.setMaxWidth(Double.MAX_VALUE);
			dtDescription.getStyleClass().add("dip-small");
			this.dtTextField = new TextField();
			dtTextField.setMaxWidth(Double.MAX_VALUE);
		}

		// selection listener to alter glyph color
		private final InvalidationListener glyphListener = (c) -> {
			updateGlyphColor();
		};

		private void updateGlyphColor() {
			if (this.currentGlyph == null) {
				return;
			}
			this.currentGlyph.setColor(
					this.selectedProperty().get()
							? UIStrategyGUI.Colors.accent_inverted
							: UIStrategyGUI.Colors.accent
			);
		}

		// listener for the named glyph itself (which might change)
		private final InvalidationListener glyphNameListener = (c) -> {
			updateCurrentGlyph();
			updateGlyphColor();
		};

		private void updateCurrentGlyph() {
			setGlyph(getNamedGlyph());
			pane.setRight(this.currentGlyph);
		}

		private NamedGlyph getNamedGlyph() {
			if (this.currentItem == null) {
				return null;
			}
			final ObjectProperty<NamedGlyph> g = this.currentItem.glyphProperty();
			if (g == null) {
				return null;
			}
			return g.get();
		}

		private void setGlyph(NamedGlyph namedGlyph) {
			if (namedGlyph != null && namedGlyph.equals(this.currentNamedGlyph)) {
				return; // reuse same/current glyph
			}
			if (this.currentGlyph != null) {
				this.selectedProperty().removeListener(glyphListener);
			}
			this.currentNamedGlyph = namedGlyph;
			if (this.currentNamedGlyph == null) {
				this.currentGlyph = null;
				return; // no glyph
			}

			// new glyph
			this.currentGlyph = UIStrategyGUI.Glyphs.newGlyph(
					this.currentNamedGlyph,
					Glyph.Size.NORMAL
			);
			this.selectedProperty().addListener(glyphListener);
		}

		@Override
		public final void updateItem(T item, boolean empty) {
			super.updateItem(item, empty);

			setText(null);
			setGraphic(null);

			if (currentItem != null) {
				dtTextField.textProperty().unbindBidirectional(currentItem.nameProperty());
				dtLabel.textProperty().unbind();
				if (currentItem.glyphProperty() != null) {
					currentItem.glyphProperty().removeListener(glyphNameListener);
				}
				if (currentItem.descriptionProperty() != null) {
					dtDescription.textProperty().unbind();
				}
			}
			currentItem = item;

			if (!empty && item != null) {
				dtTextField.textProperty().bindBidirectional(item.nameProperty());
				dtLabel.textProperty().bind(item.nameProperty());
				if (currentItem.glyphProperty() != null) {
					currentItem.glyphProperty().addListener(glyphNameListener);
				}
				if (currentItem.descriptionProperty() != null) {
					dtDescription.textProperty().bind(currentItem.descriptionProperty());
					pane.setBottom(dtDescription);
				} else {
					pane.setBottom(null);
				}

				pane.setCenter(dtLabel);
				updateCurrentGlyph();
				setGraphic(pane);
			}
		}

		@Override
		public final void startEdit() {
			super.startEdit();
			pane.addEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			dtTextField.addEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);
			pane.setCenter(dtTextField);
			pane.setRight(null);
		}

		@Override
		public final void cancelEdit() {
			super.cancelEdit();
			pane.removeEventHandler(KeyEvent.KEY_PRESSED, onEnterHandler);
			dtTextField.removeEventHandler(MouseEvent.MOUSE_CLICKED, onDoubleClickHandler);
			pane.setCenter(dtLabel);
			pane.setRight(this.currentGlyph);
		}

	}

	/**
	 * Interface of a data item.
	 */
	public static interface DataItem {

		/**
		 * The name property of the data item.
		 *
		 * @return the name property of the data item.
		 */
		public StringProperty nameProperty();

		/**
		 * The (optional) description property of the data item.
		 *
		 * @return the description property, or null.
		 */
		default StringProperty descriptionProperty() {
			return null;
		}

		/**
		 * The (optional) glyph property of the data item.
		 *
		 * @return the glyph property. Can be null, or an {@code ObjectProperty}
		 * with a null value.
		 */
		default ObjectProperty<NamedGlyph> glyphProperty() {
			return null;
		}

	}

}
