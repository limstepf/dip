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
	 * The statusBarProperty.
	 *
	 * @return the statusBarProperty.
	 */
	public ObjectProperty<Node> statusBarProperty();

	/**
	 * The menuBarProperty.
	 *
	 * @return the menuBarProperty.
	 */
	public ObjectProperty<Node> menuBarProperty();

	/**
	 * The toolBarProperty.
	 *
	 * @return the toolBarProperty.
	 */
	public ObjectProperty<Node> toolBarProperty();

	/**
	 * The optionsBarProperty.
	 *
	 * @return the optionsBarProperty.
	 */
	public ObjectProperty<Node> optionsBarProperty();

	/**
	 * The SplitPane.
	 *
	 * @return the SplitPane.
	 */
	public SplitPane getSplitPane();
}
