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

	public Parent node();

	public static ListView<Listable> newListView() {
		final ListView<Listable> listView = new ListView<>();
		listView.setMinHeight(0);
		listView.setMaxHeight(Double.MAX_VALUE);
		listView.setCellFactory((ListView<Listable> param) -> new ListableCell());
		return listView;
	}
}
