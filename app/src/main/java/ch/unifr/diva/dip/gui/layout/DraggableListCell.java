package ch.unifr.diva.dip.gui.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 * A draggable ListCell used within ListView instances. Implements drag'n'drop
 * to move around selected items (i.e. ListCells) in a ListView.
 *
 * @param <T> The type of the item contained within the ListCell.
 */
public class DraggableListCell<T> extends ListCell<T> {

	private static final PseudoClass INSERT_ABOVE = PseudoClass.getPseudoClass("insert-above");
	private static final PseudoClass INSERT_BELOW = PseudoClass.getPseudoClass("insert-below");

	/**
	 * Default constructor.
	 */
	public DraggableListCell() {
		super();
		getStyleClass().add("dip-list-cell");
		setupDraggable();
	}

	private Integer getIndex(DraggableListCell<T> item) {
		final ObservableList<T> items = item.getListView().getItems();
		return items.indexOf(item.getItem());
	}

	/**
	 * DataFormat for a set of selected indices.
	 */
	public static final DataFormat LIST_INDICES = new DataFormat("list/indices");

	/**
	 * DataFormat container for a set of selected indices.
	 *
	 * @param <T>
	 */
	public static class ListSelection<T> implements Serializable {

		public final ArrayList<Integer> indices;

		/**
		 * Default constructor.
		 *
		 * @param indices a set of indices into some kind of list.
		 */
		public ListSelection(List<Integer> indices) {
			this.indices = new ArrayList<>(indices);
		}

		/**
		 * Returns the items the selected indices refer to.
		 *
		 * @param listView the source list the indices are pointing to.
		 * @return a subset of the given listView.
		 */
		public List<T> getItems(ListView<T> listView) {
			final ArrayList<T> items = new ArrayList<>();
			for (int i : indices) {
				items.add(listView.getItems().get(i));
			}
			return items;
		}

		/**
		 * Returns the greatest index in the given selection.
		 *
		 * @return maximum index.
		 */
		public int getMaxIndex() {
			int max = -1;
			for (int i : indices) {
				if (i > max) {
					max = i;
				}
			}
			return max;
		}
	}

	private boolean isInSelection(Integer index, Dragboard db) {
		if (!db.hasContent(LIST_INDICES)) {
			return false;
		}
		final ListSelection selection = (ListSelection) db.getContent(LIST_INDICES);
		return selection.indices.contains(index);
	}

	private void setupDraggable() {
		this.setOnDragDetected(e -> onDragDetected(e));
		this.setOnDragOver(e -> onDragOver(e));
		this.setOnDragEntered(e -> onDragEntered(e));
		this.setOnDragExited(e -> onDragExited(e));
		this.setOnDragDropped(e -> onDragDropped(e));

		// OPTION: we might wanna handle drag'n'drop of external stuff, like
		// files from a file explorer (TransferMode.Copy). Or at least implement
		// a hook for such a thing?
		//this.setOnDragDone(e -> onDragDone(e));
	}

	private void onDragDetected(MouseEvent e) {
		final ListView<T> listView = this.getListView();
		final int n = listView.getSelectionModel().getSelectedIndices().size();
		if (n == 0 || getItem() == null) {
			e.consume();
			return;
		}

		final Dragboard db = this.startDragAndDrop(TransferMode.MOVE);
		final ClipboardContent content = new ClipboardContent();

		final ListSelection selection = new ListSelection(
				listView.getSelectionModel().getSelectedIndices()
		);

		content.put(LIST_INDICES, selection);
		db.setContent(content);

		e.consume();
	}

	private void onDragOver(DragEvent e) {
		final Dragboard db = e.getDragboard();

		if (!isInSelection(getIndex(this), db) && db.hasContent(LIST_INDICES)) {
			e.acceptTransferModes(TransferMode.MOVE);

			ListSelection selection = (ListSelection) db.getContent(LIST_INDICES);
			final ListView<T> listView = this.getListView();
			final List<T> list = new ArrayList<>(listView.getItems());
			final List<T> items = selection.getItems(listView);
			final T target = this.getItem();

			if (list.indexOf(target) >= selection.getMaxIndex()) {
				this.pseudoClassStateChanged(INSERT_BELOW, true);
			} else {
				this.pseudoClassStateChanged(INSERT_ABOVE, true);
			}
		}

		e.consume();
	}

	private void onDragEntered(DragEvent e) {
		final Dragboard db = e.getDragboard();

		if (!isInSelection(getIndex(this), db) && db.hasContent(LIST_INDICES)) {
			this.setOpacity(0.78);
		}

		e.consume();
	}

	private void onDragExited(DragEvent e) {
		this.setOpacity(1.0);

		this.pseudoClassStateChanged(INSERT_ABOVE, false);
		this.pseudoClassStateChanged(INSERT_BELOW, false);

		e.consume();
	}

	private void onDragDropped(DragEvent e) {
		boolean success = false;
		final Dragboard db = e.getDragboard();

		if (db.hasContent(LIST_INDICES)) {
			ListSelection selection = (ListSelection) db.getContent(LIST_INDICES);

			final ListView<T> listView = this.getListView();
			final List<T> list = new ArrayList<>(listView.getItems());
			final List<T> items = selection.getItems(listView);
			final T target = this.getItem();

			// t < idx: add above, t > idx: add below target
			final int offset = (list.indexOf(target) >= selection.getMaxIndex()) ? 1 : 0;

			// remove selected items
			list.removeAll(items);

			// reinsert above/below target
			final int targetIdx = list.indexOf(this.getItem());
			// just append if dropped onto an empty listCell at the bottom...
			if (targetIdx < 0 || (targetIdx + offset) >= list.size()) {
				list.addAll(items);
			} else {
				list.addAll(targetIdx + offset, items);
			}

			// copy back: modify original observable list
			listView.getItems().setAll(list);

			// reselect selected items
			listView.getSelectionModel().clearSelection();
			for (T item : items) {
				listView.getSelectionModel().select(item);
			}

			success = true;
		}

		e.setDropCompleted(success);
		e.consume();
	}

	/*
	 private void onDragDone(DragEvent e) {
	 if (e.getTransferMode() == TransferMode.MOVE) {
	 // MOVE
	 }

	 e.consume();
	 }
	 */
}
