package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.components.ProcessorContext;

/**
 * A resetable processor has state that can be reset.
 */
public interface Resetable extends Processor {

	/**
	 * Resets the processor. This clears any persitent processor data and all
	 * outputs, usually (re-)setting the processor state from {@code READY} (or
	 * {@code ERROR}) to {@code PROCESSING}.
	 *
	 * @param context
	 */
	public void reset(ProcessorContext context);

}
