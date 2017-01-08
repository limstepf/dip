package ch.unifr.diva.dip.gui.layout;

import javafx.scene.control.ListCell;

/**
 * Listable cell item.
 *
 * @param <T> class of the listable.
 */
public class ListableCell<T extends Listable> extends ListCell<T> {

	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);

		setText(null);
		setGraphic(empty ? null : item.node());
	}

}
