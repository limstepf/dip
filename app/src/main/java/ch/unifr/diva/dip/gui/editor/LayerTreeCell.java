package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Layer tree cell. Used by the LayersWidget that displays the processors of a
 * {@code RunnablePipeline} ordered/grouped by the stages of the pipeline.
 */
public class LayerTreeCell extends TreeCell<Layer> {

	private final BorderPane pane;
	private final VBox vbox;
	private final Label title;
	private final RadioButton visibleButton = new RadioButton();
	private Layer currentItem;

	/**
	 * Creates a new layer tree cell.
	 */
	public LayerTreeCell() {
		this.vbox = new VBox();
		this.title = new Label();
		this.title.setAlignment(Pos.CENTER_LEFT);
		this.vbox.getChildren().addAll(title);
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

	private Glyph glyph;

	private void setGlyph(Layer item, boolean empty) {
		if (empty || item.getHiddenGlyph() == null) {
			glyph = null;
			this.focusedProperty().removeListener(glyphListener);
			this.selectedProperty().removeListener(glyphListener);
		} else {
			glyph = UIStrategyGUI.Glyphs.newGlyph(item.getHiddenGlyph(), Glyph.Size.MEDIUM);
			BorderPane.setAlignment(glyph, Pos.TOP_CENTER);
			BorderPane.setMargin(glyph, new Insets(2, UIStrategyGUI.Stage.insets, 0, -15));
			this.focusedProperty().addListener(glyphListener);
			this.selectedProperty().addListener(glyphListener);
		}

		this.pane.setLeft(glyph);
	}

	private final InvalidationListener glyphListener = (c) -> updateColor();

	private void updateColor() {
		if (glyph == null) {
			return;
		}
		if (isFocused() || isSelected()) {
			glyph.setColor(Color.WHITE);
		} else {
			glyph.setColor(UIStrategyGUI.Colors.accent);
		}
	}

	@Override
	public void updateItem(Layer item, boolean empty) {
		super.updateItem(item, empty);
		setText(null);
		setGraphic(null);

		if (currentItem != null) {
			visibleButton.visibleProperty().unbind();
			visibleButton.selectedProperty().unbindBidirectional(currentItem.visibleProperty());
			pane.disableProperty().unbind();
		}

		setGlyph(item, empty);

		if (!empty) {
			currentItem = item;
			visibleButton.visibleProperty().bind(Bindings.not(currentItem.emptyProperty()));
			visibleButton.selectedProperty().bindBidirectional(currentItem.visibleProperty());
			// TODO: no! we can't just disable all of it! :D
			pane.disableProperty().bind(Bindings.not(currentItem.passiveVisibleProperty()));
			title.setText(currentItem.getHiddenName());
			setupVbox();

			setGraphic(pane);
		}
	}

}
