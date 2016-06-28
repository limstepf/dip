package ch.unifr.diva.dip.gui.editor;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Layer tree cell.
 */
public class LayerTreeCell extends TreeCell<Layer> {

	private final BorderPane pane;
	private final VBox vbox;
	private final Label title;
	private final RadioButton visibleButton;
	private Layer currentItem;

	public LayerTreeCell() {
		this.vbox = new VBox();
		this.title = new Label();
		this.title.setAlignment(Pos.CENTER_LEFT);
		this.vbox.getChildren().addAll(title);
		this.visibleButton = new RadioButton();
		this.pane = new BorderPane();
		this.pane.setCenter(this.vbox);
		this.pane.setRight(this.visibleButton);
	}

	private void setupVbox() {
		this.vbox.getChildren().setAll(title);
		for (LayerExtension ext : currentItem.getHiddenLayerExtensions()) {
			this.vbox.getChildren().add(ext.getComponent());
		}
	}

	private boolean isLayerGroup(Layer item) {
		return (item instanceof LayerGroup);
	}

	@Override
	public void updateItem(Layer item, boolean empty) {
		super.updateItem(item, empty);
		setText(null);
		setGraphic(null);

		if (currentItem != null) {
			visibleButton.selectedProperty().unbindBidirectional(currentItem.visibleProperty());
			pane.disableProperty().unbind();
		}

		if (!empty) {
			currentItem = item;
			visibleButton.selectedProperty().bindBidirectional(currentItem.visibleProperty());
			// TODO: no! we can't just disable all of it! :D
			pane.disableProperty().bind(Bindings.not(currentItem.passiveVisibleProperty()));
			title.setText(currentItem.getHiddenName());
			setupVbox();

			setGraphic(pane);
		}
	}
}
