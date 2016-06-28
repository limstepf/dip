
package ch.unifr.diva.dip.gui.layout;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A Lane (or a fancy HBox).
 */
public class Lane extends HBox {

	/**
	 * Default constructor.
	 */
	public Lane() {
		super();
		setAlignment(Pos.CENTER);
		getStyleClass().add("dip-lane");
	}

	/**
	 * Adds node(s) to the lane.
	 * @param children a bunch of nodes.
	 */
	public void add(Node... children) {
		getChildren().addAll(children);
	}

	/**
	 * Adds a node with defined horizontal grow priority to the lane.
	 * @param node a node
	 * @param hgrow the horizontal grow priority.
	 */
	public void add(Node node, Priority hgrow) {
		getChildren().add(node);
		HBox.setHgrow(node, hgrow);
	}
	
}
