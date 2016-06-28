
package ch.unifr.diva.dip.gui.editor;

import ch.unifr.diva.dip.core.ui.Localizable;
import ch.unifr.diva.dip.gui.AbstractWidget;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Layers widget.
 */
public class LayersWidget extends AbstractWidget implements Localizable {

	private final LayerTreeView view;

	public LayersWidget() {
		super();

		this.view = new LayerTreeView();
		setWidget(this.view);
		setTitle(localize("pipeline"));
	}

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
