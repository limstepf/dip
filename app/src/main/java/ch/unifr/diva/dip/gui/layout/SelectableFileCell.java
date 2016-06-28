package ch.unifr.diva.dip.gui.layout;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;

/**
 * A SelectableFileCell is a CheckBoxListCell whose items also can be disabled.
 * Disabled cells can't be selected. The idea here is to have a list of files to
 * select from, with the additional option to indicate that some files can not
 * be selected (e.g. in case of an unsuitable file type/extension).
 *
 * @see SelectableFile
 */
public class SelectableFileCell extends DraggableListCell<SelectableFile> {

	private final BorderPane borderPane;
	private final CheckBox checkBox;
	private final Label label;

	// cells are reused by a ListView, hence we have to keep references to
	// the properties of the last item to unbind before binding to a new item.
	private ObservableValue<Boolean> selectedProperty;
	private ObservableValue<Boolean> disableProperty;

	/**
	 * Creates a default SelectableFileCell.
	 */
	public SelectableFileCell() {
		this.checkBox = new CheckBox();
		this.label = new Label();
		this.label.setAlignment(Pos.CENTER_LEFT);
		this.label.setMaxWidth(Double.MAX_VALUE);
		this.borderPane = new BorderPane();
		this.borderPane.setLeft(checkBox);
		this.borderPane.setCenter(label);
		this.getStyleClass().add("check-box-list-cell");
		this.setAlignment(Pos.CENTER_LEFT);
		this.setContentDisplay(ContentDisplay.LEFT);
		this.setGraphic(null);
		this.setText(null);
	}

	@Override
	protected void updateItem(SelectableFile item, boolean empty) {
		super.updateItem(item, empty);

		if (!empty) {
			this.setGraphic(borderPane);
			this.label.setText(item.toString());

			// disabled stuff can't be selected
			if (item.isDisable()) {
				item.setSelected(false);
			}

			// unbind old item
			if (selectedProperty != null) {
				checkBox.selectedProperty().unbindBidirectional((BooleanProperty) selectedProperty);
			}
			if (disableProperty != null) {
				borderPane.disableProperty().unbindBidirectional((BooleanProperty) disableProperty);
			}

			// update references
			selectedProperty = item.selectedProperty();
			disableProperty = item.disableProperty();

			// bind to new item
			checkBox.selectedProperty().bindBidirectional((BooleanProperty) selectedProperty);
			borderPane.disableProperty().bindBidirectional((BooleanProperty) disableProperty);
		} else {
			this.setGraphic(null);
		}
	}
}
