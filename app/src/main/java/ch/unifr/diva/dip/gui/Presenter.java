package ch.unifr.diva.dip.gui;

import javafx.scene.Parent;

/**
 * Presenter interface.
 */
public interface Presenter {
	/**
	 * Get the view (some JavaFX node) of a component.
	 * @return The view (or root node) of the component.
	 */
	public Parent getComponent();
}
