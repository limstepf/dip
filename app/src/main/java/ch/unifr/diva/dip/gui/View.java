package ch.unifr.diva.dip.gui;

import javafx.scene.Parent;

/**
 * View interface.
 */
public interface View {

	/**
	 * Get the root node of the view.
	 *
	 * @return The root node of the view.
	 */
	public Parent getComponent();
}
