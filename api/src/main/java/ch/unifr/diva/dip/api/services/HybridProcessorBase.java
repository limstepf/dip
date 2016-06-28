package ch.unifr.diva.dip.api.services;

/**
 * HybridProcessorBase already implements some common bits of the
 * {@code Processor} interface, offers some helper methods, and implements the
 * {@code Processable} and {@code Editable} interfaces.
 */
public abstract class HybridProcessorBase extends ProcessorBase implements Processable, Editable {

	public HybridProcessorBase(String name) {
		super(name);
	}

}
