package ch.unifr.diva.dip.gui.pe;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.gui.AbstractWidget;
import ch.unifr.diva.dip.osgi.ServiceMonitor;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ProcessorsWidget offers processors to be dragged into the Pipeline Editor.
 * TODO: a TreeView (level 1: bundle/category, level 2: processor) might work better here, eh?
 */
public class ProcessorsWidget extends AbstractWidget {

	private static final Logger log = LoggerFactory.getLogger(ProcessorsWidget.class);
	private final View view;
	private final ServiceMonitor services;

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

	public static class ProcessorListCell extends ListCell<ServiceMonitor.Service<Processor>> {

		/**
		 * DataFormat for a set of selected indices.
		 */
		public static final DataFormat OSGI_SERVICE_PROCESSOR = new DataFormat("dip-service/processor");

		private final BorderPane pane = new BorderPane();
		private final Label name = new Label();
		private final Label description = new Label();

		public ProcessorListCell() {
			name.setMaxWidth(Double.MAX_VALUE);
			name.setAlignment(Pos.CENTER_LEFT);
			description.setMaxWidth(Double.MAX_VALUE);
			description.setAlignment(Pos.CENTER_LEFT);
			description.getStyleClass().add("dip-small");
			pane.setCenter(name);
			pane.setBottom(description);

			setupDraggable();
		}

		private void setupDraggable() {
			this.setOnDragDetected(e -> onDragDetected(e));
//		this.setOnDragOver(e -> onDragOver(e));
//		this.setOnDragEntered(e -> onDragEntered(e));
//		this.setOnDragExited(e -> onDragExited(e));
//		this.setOnDragDropped(e -> onDragDropped(e));
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

			final int index = listView.getSelectionModel().getSelectedIndex();
			content.put(OSGI_SERVICE_PROCESSOR, listView.getItems().get(index).pid);
			db.setContent(content);

			e.consume();
		}

		private void onDragOver(DragEvent e) {
			final Dragboard db = e.getDragboard();

		// accept drop here simply to indicate dragging, even if it results in
			// a no-op (i.e. the dragging is aborted)
			if (db.hasContent(OSGI_SERVICE_PROCESSOR)) {
				e.acceptTransferModes(TransferMode.COPY);
			}

			e.consume();
		}

		private void onDragEntered(DragEvent e) {
			e.consume();
		}

		private void onDragExited(DragEvent e) {
			e.consume();
		}

		private void onDragDropped(DragEvent e) {
			e.setDropCompleted(true);
			e.consume();
		}

		private void onDragDone(DragEvent e) {
			e.consume();
		}

		@Override
		public final void updateItem(ServiceMonitor.Service<Processor> p, boolean empty) {
			super.updateItem(p, empty);
			setText(null);
			setGraphic(null);

			if (!empty) {
				name.setText(p.service.name());
				description.setText(p.pid);
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

			listView.setItems(services.processors());
			this.getChildren().addAll(listView);
		}
	}

}
