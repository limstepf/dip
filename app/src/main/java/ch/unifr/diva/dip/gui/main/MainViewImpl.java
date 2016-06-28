package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.gui.AbstractView;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

/**
 * MainView Implementation.
 */
public class MainViewImpl extends AbstractView<BorderPane> implements MainView {

	private final SplitPane splitPane = new SplitPane();

	public MainViewImpl() {
		super(new BorderPane());
		splitPane.setOrientation(Orientation.HORIZONTAL);
		root.setCenter(splitPane);
	}

	@Override
	public ObjectProperty<Node> statusBarProperty() {
		return root.bottomProperty();
	}

	@Override
	public ObjectProperty<Node> menuBarProperty() {
		return root.topProperty();
	}

	@Override
	public SplitPane getSplitPane() {
		return splitPane;
	}

}
