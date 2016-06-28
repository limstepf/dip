
package ch.unifr.diva.dip.gui.layout;

import javafx.scene.control.ListCell;

/**
 *
 */
public class ListableCell extends ListCell<Listable> {

	@Override
	public void updateItem(Listable item, boolean empty) {
		super.updateItem(item, empty);

		setText(null);
		setGraphic(empty ? null : item.node());
	}

}
