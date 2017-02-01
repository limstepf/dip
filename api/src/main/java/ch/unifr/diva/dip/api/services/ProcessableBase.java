package ch.unifr.diva.dip.api.services;

/**
 * ProcessableBase already implements some common bits of the {@code Processor}
 * interface, offers some helper methods, and implements the {@code Processable}
 * interface.
 */
public abstract class ProcessableBase extends ProcessorBase implements Processable {

	public ProcessableBase(String name) {
		super(name);
	}

	/*
	 * HybridProcessorBase extends from EditableBase, not from ProcessableBase.
	 * So we might wanna have anything that's implemented here also (duplicated)
	 * over in HybridProcessorBase.
	 */
}
