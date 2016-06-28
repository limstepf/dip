package ch.unifr.diva.dip.core.services.api;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.services.Processor;

/**
 * A host processor isn't registered as an OSGi service and get's instantiated
 * directly by the host application. Host processors are primarily used as
 * sources (or generators) and maybe sinks (or writers). To do so, the
 * constructors of host processors usually get some more application data passed
 * along than ordinary processors by means of a {@code HostProcessorContext}.
 */
public interface HostProcessor extends Processor {

	@Override
	default Processor newInstance(ProcessorContext context) {
		return null; // no-op
	}

	@Override
	default void init(ProcessorContext context) {
		// no-op
	}

	/**
	 * Returns a new processor instance for the given context.
	 *
	 * @param context the host processor context.
	 * @return a new instance of a host processor.
	 */
	public HostProcessor newInstance(HostProcessorContext context);

	/**
	 * Hook method called as soon as a new instance of a processor has been
	 * fully initialized by its wrapper.
	 *
	 * @param context the host processor context if the processor is part of a
	 * {@code RunnablePipeline}, or null otherwise (if for a {@code Pipeline} in
	 * the pipeline editor).
	 */
	public void init(HostProcessorContext context);

}
