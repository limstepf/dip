package ch.unifr.diva.dip.core.services.api;

import ch.unifr.diva.dip.api.components.EditorLayerGroup;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.core.model.ProjectPage;

/**
 * Context passed to host processors/services.
 */
public class HostProcessorContext {

	/**
	 * Application wide thread pool/executor service.
	 */
	public final DipThreadPool threadPool;
	/**
	 * The project page.
	 */
	public final ProjectPage page;
	/**
	 * The processor's editor layer group.
	 */
	public final EditorLayerGroup layer;

	/**
	 * Creates a new host processor context.
	 *
	 * @param threadPool application wide thread pool/executor service.
	 * @param page the project page.
	 * @param layer the processor's layer group.
	 */
	public HostProcessorContext(DipThreadPool threadPool, ProjectPage page, EditorLayerGroup layer) {
		this.threadPool = threadPool;
		this.page = page;
		this.layer = layer;
	}

}
