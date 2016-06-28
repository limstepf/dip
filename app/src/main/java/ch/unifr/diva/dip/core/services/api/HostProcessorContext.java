
package ch.unifr.diva.dip.core.services.api;

import ch.unifr.diva.dip.api.components.EditorLayerGroup;
import ch.unifr.diva.dip.core.model.ProjectPage;

/**
 * Context passed to host processors/services.
 */
public class HostProcessorContext {
	public final ProjectPage page;
	public final EditorLayerGroup layer;

	public HostProcessorContext(ProjectPage page, EditorLayerGroup layer) {
		this.page = page;
		this.layer = layer;
	}

}
