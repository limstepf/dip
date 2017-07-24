package ch.unifr.diva.dip.api.services;

/**
 * Hybrid processor base already implements some common bits of the
 * {@code Processor} interface, offers some helper methods, and implements the
 * {@code Processable} and {@code Editable} interfaces.
 */
public abstract class HybridProcessorBase extends EditableBase implements Processable {

	/**
	 * Creates a new hybrid processor base processor.
	 *
	 * @param name the name of the processor.
	 */
	public HybridProcessorBase(String name) {
		super(name);
	}

	/*
	 * We're extending from EditableBase, so we might wanna implement/duplicate
	 * anything that is additionally implemented in ProcessableBase here too.
	 */
}
