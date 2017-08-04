package ch.unifr.diva.dip.api.components;

import javafx.application.HostServices;
import javafx.scene.Node;

/**
 * Processor documentation interface.
 */
public interface ProcessorDocumentation {

	/**
	 * Sets/registers the FX application host services. Needed to open
	 * hyperlinks in a browser.
	 *
	 * @param hostServices the FX application host services
	 */
	public void setHostServices(HostServices hostServices);

	/**
	 * Returns the (root) node of the processor documentation.
	 *
	 * @return the (root) node of the processor documentation.
	 */
	public Node getNode();

}
