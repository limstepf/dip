package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.gui.AbstractView;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * MainView Implementation.
 */
public class MainViewImpl extends AbstractView<BorderPane> implements MainView {

	private final SplitPane splitPane = new SplitPane();
	private final VBox top = new VBox();

	private Node menuBarNode = null;
	private Node optionsBarNode = null;

	private final ObjectProperty<Node> menuBarProperty = new SimpleObjectProperty<Node>() {
		@Override
		public void set(Node node) {
			super.set(node);
			menuBarNode = node;
			repaintTop();
		}
	};

	private final ObjectProperty<Node> optionsBarProperty = new SimpleObjectProperty<Node>() {
		@Override
		public void set(Node node) {
			super.set(node);
			optionsBarNode = node;
			repaintTop();
		}
	};

	private void repaintTop() {
		top.getChildren().clear();
		if (menuBarNode != null) {
			top.getChildren().add(menuBarNode);
		}
		if (optionsBarNode != null) {
			top.getChildren().add(optionsBarNode);
		}
	}

	public MainViewImpl() {
		super(new BorderPane());
		splitPane.setOrientation(Orientation.HORIZONTAL);
		root.setTop(top);
		root.setCenter(splitPane);
	}

	@Override
	public ObjectProperty<Node> statusBarProperty() {
		return root.bottomProperty();
	}

	@Override
	public ObjectProperty<Node> menuBarProperty() {
		return this.menuBarProperty;
	}

	@Override
	public ObjectProperty<Node> toolBarProperty() {
		return root.leftProperty();
	}

	@Override
	public ObjectProperty<Node> optionsBarProperty() {
		return this.optionsBarProperty;
	}

	@Override
	public SplitPane getSplitPane() {
		return splitPane;
	}

}
