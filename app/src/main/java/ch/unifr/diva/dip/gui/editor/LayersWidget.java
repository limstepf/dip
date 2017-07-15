package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.eventbus.events.ProcessorNotification;
import ch.unifr.diva.dip.gui.AbstractWidget;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Layers widget. The layers widget displays the processors of a
 * {@code RunnablePipeline} ordered/grouped by the stages of the pipeline.
 */
public class LayersWidget extends AbstractWidget {

	private final ApplicationHandler handler;
	private final LayerTreeView view;

	/**
	 * Creates a new layers widget.
	 *
	 * @param handler the application handler.
	 */
	public LayersWidget(ApplicationHandler handler) {
		super();

		this.handler = handler;
		this.view = new LayerTreeView();
		setWidget(this.view);
		setTitle(localize("pipeline"));

		// listen if a layer owned by a processor has been selected, thereby
		// selecting that processor (e.g. to display its associated tools).
		this.view.treeView.getSelectionModel().selectedItemProperty().addListener((e) -> {
			final TreeItem<Layer> item = this.view.treeView.getSelectionModel().getSelectedItem();
			final int owner;
			if (item == null) {
				owner = -1;
			} else {
				final Layer layer = item.getValue();
				owner = layer.getOwnerProcessorId();
			}
			if (owner >= 0) {
				// send notification that the layer of a processor (and thereby the
				// processor itself) has been selected
				this.handler.eventBus.post(
						new ProcessorNotification(ProcessorNotification.Type.SELECTED, owner)
				);
			} else {
				// send notification that no layer of a processor is selected any
				// longer
				this.handler.eventBus.post(
						new ProcessorNotification(ProcessorNotification.Type.SELECTED, -1)
				);
			}
		});
	}

	/**
	 * Sets the root tree item of the layers widget.
	 *
	 * @param root the new root tree item.
	 */
	public void setRoot(TreeItem<Layer> root) {
		this.view.setRoot(root);
	}

	/**
	 * Expands all layers.
	 */
	public void expandAll() {
		this.view.expandAll();
	}

	/**
	 * Layer tree view.
	 */
	public static class LayerTreeView extends VBox {

		private final TreeView<Layer> treeView = new TreeView<>();

		/**
		 * Creates a new LayerTreeView. The root needs to be set manually with a
		 * call to {@code setRoot()}.
		 */
		public LayerTreeView() {
			setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(treeView, Priority.ALWAYS);

			treeView.setPrefHeight(0);
			treeView.setMaxHeight(Double.MAX_VALUE);
			treeView.setShowRoot(false);
			treeView.setCellFactory((TreeView<Layer> param) -> new LayerTreeCell());

			this.getChildren().addAll(treeView);
		}

		/**
		 * Sets/updates the root layer.
		 *
		 * @param root the new root layer.
		 */
		public void setRoot(TreeItem<Layer> root) {
			this.treeView.setRoot(root);
		}

		/**
		 * Expands all layers.
		 */
		public void expandAll() {
			expand(this.treeView.getRoot());
		}

		private void expand(TreeItem<Layer> layer) {
			if (layer != null && !layer.isLeaf()) {
				layer.setExpanded(true);
				for (TreeItem<Layer> child : layer.getChildren()) {
					expand(child);
				}
			}
		}
	}

}
