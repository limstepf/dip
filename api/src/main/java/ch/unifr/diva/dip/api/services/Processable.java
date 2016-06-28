package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.components.ProcessorContext;

/**
 * A processable processor automatically processes its inputs without further
 * user interaction. Nevertheless, a processable processor can also be editable
 * (e.g. to manually correct the automatically processed result).
 */
public interface Processable extends Resetable {

	/**
	 * Processes the inputs and sets/serves the outputs.
	 *
	 * @param context the processor context.
	 */
	public void process(ProcessorContext context);

}
