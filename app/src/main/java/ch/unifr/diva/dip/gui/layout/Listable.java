package ch.unifr.diva.dip.gui.layout;

import javafx.scene.Parent;
import javafx.scene.control.ListView;

/**
 * Interface to populate quick'n'dirty ListableCell's. Probably shouldn't be
 * used for lists with a larger amount of items, since it defeats the whole
 * reuse cell-item resources stuff... Then again, don't worry if you expect
 * pretty much all items to be displayed anyways.
 */
public interface Listable {

	/**
	 * Returns the node/body of the listable cell.
	 *
	 * @return the node/body of the listable cell.
	 */
	public Parent node();

	/**
	 * Creates a new list view for listables.
	 *
	 * @param <T> the class of the listable.
	 * @return a new ListView for listables.
	 */
	public static <T extends Listable> ListView<T> newListView() {
		final ListView<T> listView = new ListView<>();
		listView.setMinHeight(0);
		listView.setMaxHeight(Double.MAX_VALUE);
		listView.setCellFactory((ListView<T> param) -> new ListableCell<>());
		return listView;
	}

}
