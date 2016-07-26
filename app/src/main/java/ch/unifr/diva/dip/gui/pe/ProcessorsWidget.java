package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.ui.Glyph;
import ch.unifr.diva.dip.api.ui.NamedGlyph;
import ch.unifr.diva.dip.core.model.ProcessorWrapper;
import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.osgi.ServiceMonitor;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ProcessorsWidget offers processors to be dragged into the Pipeline
 * Editor. TODO: a TreeView (level 1: bundle/category, level 2: processor) might
 * work better here, eh?
 */
public class ProcessorsWidget extends AbstractWidget {

	private static final Logger log = LoggerFactory.getLogger(ProcessorsWidget.class);
	private final View view;
	private final ServiceMonitor services;

	/**
	 * Creates a new processor widget.
	 *
	 * @param label label of the widget.
	 * @param services the processor service monitor.
	 */
	public ProcessorsWidget(String label, ServiceMonitor services) {
		super();

		this.services = services;
		this.view = new View(services);

		setWidget(view);
		setTitle(label);
	}

	public void setDisable(boolean disable) {
		this.view.setDisable(disable);
	}

	/**
	 * Processor list cell.
	 */
	public static class ProcessorListCell extends ListCell<ServiceMonitor.Service<Processor>> {

		/**
		 * DataFormat for a set of selected indices.
		 */
		public static final DataFormat OSGI_SERVICE_PROCESSOR = new DataFormat("dip-service/processor");

		private final BorderPane pane = new BorderPane();
		private final VBox vbox = new VBox();
		private final Label name = new Label();
		private final Label description = new Label();
		private NamedGlyph namedGlyph;
		private Glyph glyph;

		public ProcessorListCell() {
			name.setMaxWidth(Double.MAX_VALUE);
			name.setAlignment(Pos.CENTER_LEFT);
			description.setMaxWidth(Double.MAX_VALUE);
			description.setAlignment(Pos.CENTER_LEFT);
			description.getStyleClass().add("dip-small");
			pane.setCenter(vbox);
			vbox.setPadding(new Insets(0, 0, 0, UIStrategyGUI.Stage.insets + 2));
			vbox.getChildren().setAll(name, description);

			setupDraggable();
		}

		private void setupDraggable() {
			this.setOnDragDetected(e -> onDragDetected(e));
			this.setOnDragDone(e -> onDragDone(e));
		}

		private void onDragDetected(MouseEvent e) {
			final ListView<ServiceMonitor.Service<Processor>> listView = this.getListView();
			final int n = listView.getSelectionModel().getSelectedIndices().size();
			if (n == 0 || getItem() == null) {
				e.consume();
				return;
			}

			final Dragboard db = this.startDragAndDrop(TransferMode.COPY);
			final ClipboardContent content = new ClipboardContent();

			db.setDragView(getDragView());

			final int index = listView.getSelectionModel().getSelectedIndex();
			content.put(OSGI_SERVICE_PROCESSOR, listView.getItems().get(index).pid);
			db.setContent(content);

			e.consume();
		}

		private void onDragDone(DragEvent e) {
			e.consume();
		}

		private final static SnapshotParameters snapshotParams;
		static {
			snapshotParams = new SnapshotParameters();
			snapshotParams.setFill(Color.TRANSPARENT);
		}

		private Image getDragView() {
			final Glyph g = UIStrategyGUI.newGlyph(namedGlyph, Glyph.Size.HUGE);
			g.setBackground(Color.TRANSPARENT);
			g.setColor(Color.WHITE);
			final Scene s = new Scene(g, Color.TRANSPARENT);
			final WritableImage image = g.snapshot(snapshotParams, null);
			return image;
		}

		private Glyph getGlyph(Processor p) {
			namedGlyph = ProcessorWrapper.glyph(p);
			glyph = UIStrategyGUI.newGlyph(namedGlyph, Glyph.Size.MEDIUM);
			updateColor();
			BorderPane.setAlignment(glyph, Pos.TOP_CENTER);
			return glyph;
		}

		private final InvalidationListener glyphListener = (c) -> updateColor();

		private void updateColor() {
			if (isFocused() || isSelected()) {
				glyph.setColor(Color.WHITE);
			} else {
				glyph.setColor(UIStrategyGUI.Colors.accent);
			}
		}

		@Override
		public final void updateItem(ServiceMonitor.Service<Processor> p, boolean empty) {
			super.updateItem(p, empty);
			setText(null);
			setGraphic(null);

			this.focusedProperty().removeListener(glyphListener);
			this.selectedProperty().removeListener(glyphListener);

			if (!empty) {
				this.focusedProperty().addListener(glyphListener);
				this.selectedProperty().addListener(glyphListener);

				name.setText(p.service.name());
				description.setText(p.pid);
				pane.setLeft(getGlyph(p.service));
				setGraphic(pane);

			}
		}
	}

	public static class View extends VBox {

		private final ListView<ServiceMonitor.Service<Processor>> listView = new ListView<>();

		public View(ServiceMonitor services) {
			setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(listView, Priority.ALWAYS);

			listView.setEditable(false);
			listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			listView.setPrefHeight(0);
			listView.setMaxHeight(Double.MAX_VALUE);
			listView.setCellFactory((ListView<ServiceMonitor.Service<Processor>> param) -> new ProcessorListCell());

			listView.setItems(services.services());
			listView.getSelectionModel().select(-1);
			this.getChildren().addAll(listView);
		}
	}

}
