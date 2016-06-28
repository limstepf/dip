package ch.unifr.diva.dip.gui.main;

import ch.unifr.diva.dip.gui.View;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

/**
 * MainView interface.
 */
public interface MainView extends View {

	/**
	 * Returns the statusBarProperty.
	 *
	 * @return the statusBarProperty.
	 */
	public ObjectProperty<Node> statusBarProperty();

	/**
	 * Returns the menuBarProperty.
	 *
	 * @return the menuBarProperty.
	 */
	public ObjectProperty<Node> menuBarProperty();

	/**
	 * Returns the SplitPane.
	 *
	 * @return the SplitPane.
	 */
	public SplitPane getSplitPane();
}
