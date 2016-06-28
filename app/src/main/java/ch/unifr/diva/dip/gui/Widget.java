
package ch.unifr.diva.dip.gui;

import javafx.scene.Node;

/**
 * Widget interface.
 */
public interface Widget extends Presenter {
	public String getTitle();
	public void setHandle(Node handle);
}
