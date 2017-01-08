package ch.unifr.diva.dip.gui.layout;

import ch.unifr.diva.dip.core.ui.UIStrategyGUI;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * A vertically split pane with two {@code VBox}es.
 */
public class VerticalSplitPane {

	protected final BorderPane root;
	protected final VBox leftBox;
	protected final VBox rightBox;

	/**
	 * Creates a new vertically split pane.
	 */
	public VerticalSplitPane() {
		super();
		final double b = UIStrategyGUI.Stage.insets;
		root = new BorderPane();
		leftBox = newVBox(b);
		leftBox.setPadding(new Insets(0, b * 2, 0, 0));
		rightBox = newVBox(b);
		root.setLeft(leftBox);
		root.setCenter(rightBox);
	}

	protected static VBox newVBox(double spacing) {
		final VBox vbox = new VBox();
		vbox.setMaxHeight(Double.MAX_VALUE);
		vbox.setMaxWidth(Double.MAX_VALUE);
		vbox.setSpacing(spacing);
		return vbox;
	}

	/**
	 * Return the root node of the pane.
	 *
	 * @return the root node of the pane.
	 */
	public Node getNode() {
		return root;
	}

	/**
	 * Returns the children of the left side.
	 *
	 * @return the children of the left side.
	 */
	public ObservableList<Node> getLeftChildren() {
		return leftBox.getChildren();
	}

	/**
	 * Returns the children of the right side.
	 *
	 * @return the children of the right side.
	 */
	public ObservableList<Node> getRightChildren() {
		return rightBox.getChildren();
	}

}
