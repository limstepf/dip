package ch.unifr.diva.dip.api.services;

/**
 * Processable base already implements some common bits of the {@code Processor}
 * interface, offers some helper methods, and implements the {@code Processable}
 * interface.
 */
public abstract class ProcessableBase extends ProcessorBase implements Processable {

	/**
	 * Creates a new processable base processor.
	 *
	 * @param name the name of the processor.
	 */
	public ProcessableBase(String name) {
		super(name);
	}

	/*
	 * HybridProcessorBase extends from EditableBase, not from ProcessableBase.
	 * So we might wanna have anything that's implemented here also (duplicated)
	 * over in HybridProcessorBase.
	 */
}
