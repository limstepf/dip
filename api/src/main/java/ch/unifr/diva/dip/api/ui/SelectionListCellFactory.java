package ch.unifr.diva.dip.api.ui;

import java.util.BitSet;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * List cell factory for final lists with a single selection model. This is
 * intended to be used by lists with a SingleSelectionModel (e.g. ComboBox).
 * Single cells can be disabled, or marked s.t. a visual separator (css only)
 * will be shown below that cell.
 *
 * <p>
 * Note that this factory can only be used on final lists, that is lists that
 * don't change their content (or items/cells their indices respectively). Also
 * make sure to have a new, dedicated factory per control.
 *
 * @param <T>
 */
public class SelectionListCellFactory<T> implements Callback<ListView<T>, ListCell<T>> {

	private ListView listView = null;
	// TODO: if support for dynamic/modifiable lists is ever needed we'd need to
	// listen to changes and bookkeep these indices
	private final BitSet separators = new BitSet();
	private final BitSet disabled = new BitSet();

	/**
	 * Adds a separator to/below this item/cell.
	 *
	 * @param index index of the item/cell.
	 */
	public void addSeparator(Integer index) {
		this.separators.set(index);
	}

	/**
	 * Removes a reparator from an item/cell.
	 *
	 * @param index index of the item/cell.
	 */
	public void removeSeparator(Integer index) {
		this.separators.clear(index);
	}

	/**
	 * Disables/enables a list item/cell. Disabled items/cells can not be
	 * selected.
	 *
	 * @param index index of the item/cell.
	 * @param disable True to disable, False to enable.
	 */
	public void setDisable(Integer index, boolean disable) {
		if (disable) {
			this.disabled.set(index);
		} else {
			this.disabled.clear(index);
		}

		// update item
		if (this.listView != null) {
			this.listView.getItems().set(index, listView.getItems().get(index));
		}
	}

	@Override
	public ListCell<T> call(ListView<T> param) {
		// init list on first request for a cell
		if (this.listView == null) {
			this.listView = param;
			// register change listener to prevent user from selecting disabled
			// cells...
			this.listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
				final int n = this.listView.getItems().size();
				final int i1 = (newIndex.intValue() < 0 || newIndex.intValue() >= n)
						? n - 1
						: newIndex.intValue();
				final int i0 = (oldIndex.intValue() < 0) ? i1 : oldIndex.intValue();

				// IndexOutOfBoundsException here without Platform.runLater :|
				// see: https://bugs.openjdk.java.net/browse/JDK-8093905
				// "Changes to the list during the change events are simply disallowed
				// since they break the List for the subsequent listeners. [...]
				// If you really need to change the List as a result of some other
				// change Event, you can use Platform.runLater() to do the change."
				Platform.runLater(() -> {
					if (this.disabled.get(i1)) {
						if (i1 > i0) {
							for (int i = i1; i < n; i++) {
								if (!this.disabled.get(i)) {
									this.listView.getSelectionModel().select(i);
									return;
								}
							}
						} else {
							for (int i = i1; i >= 0; i--) {
								if (!this.disabled.get(i)) {
									this.listView.getSelectionModel().select(i);
									return;
								}
							}
						}

						if (!this.disabled.get(i0)) {
							this.listView.getSelectionModel().select(i0);
						} else if (!this.disabled.get(0)) {
							// try to fallback to first item if that one isn't disabled
							this.listView.getSelectionModel().select(0);
						}
					}
				});
			});
		}
		return new Cell(separators, disabled);
	}

	/**
	 * Extended cell.
	 *
	 * @param <T>
	 */
	public static class Cell<T> extends ListCell<T> {

		private final BitSet separators;
		private final BitSet disabled;

		public Cell(BitSet separators, BitSet disabled) {
			this.separators = separators;
			this.disabled = disabled;
		}

		@Override
		public void updateItem(T item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
				this.setDisable(false);
				this.getStyleClass().remove("dip-separator");
			} else {
				if (item instanceof String) {
					setText((String) item);
					setGraphic(null);
				} else if (item instanceof Node) {
					setText(null);
					setGraphic((Node) item);
				}

				if (this.separators.get(getIndex())) {
					this.getStyleClass().add("dip-separator");
				} else {
					this.getStyleClass().remove("dip-separator");
				}

				// just disabling a cell isn't good enough: in a combobox selecting
				// the item with a mouse doesn't work any longer (good), but there
				// is no visual indication, and worse: we still can select disabled
				// items by keyboard-navigation/arrows (super bad).
				final boolean disable = this.disabled.get(getIndex());
				this.setDisable(disable);
				if (disable) {
					this.getStyleClass().add("dip-disabled");
				} else {
					this.getStyleClass().remove("dip-disabled");
				}
			}
		}
	}

}
