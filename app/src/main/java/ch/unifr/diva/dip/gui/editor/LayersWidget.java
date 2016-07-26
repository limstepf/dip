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
	 * Layer tree view.
	 */
	public static class LayerTreeView extends VBox {

		private final TreeView<Layer> treeView = new TreeView<>();

		public LayerTreeView() {
			setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(treeView, Priority.ALWAYS);

			treeView.setPrefHeight(0);
			treeView.setMaxHeight(Double.MAX_VALUE);
			treeView.setShowRoot(false);
			treeView.setCellFactory((TreeView<Layer> param) -> new LayerTreeCell());

			this.getChildren().addAll(treeView);
		}

		public void setRoot(TreeItem<Layer> root) {
			this.treeView.setRoot(root);
		}
	}

}
