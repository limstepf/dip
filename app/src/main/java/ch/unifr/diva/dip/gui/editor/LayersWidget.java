package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.gui.AbstractWidget;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Layers widget. The layers widget displays the processors of a
 * {@code RunnablePipeline} ordered/grouped by the stages of the pipeline.
 */
public class LayersWidget extends AbstractWidget implements Localizable {

	private final LayerTreeView view;

	/**
	 * Creates a new layers widget.
	 */
	public LayersWidget() {
		super();

		this.view = new LayerTreeView();
		setWidget(this.view);
		setTitle(localize("pipeline"));
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
