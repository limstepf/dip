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

	/**
	 * Throws an {@code InterruptedException} if the current thread's
	 * interrupted flag is set. May be used in the {@code process()} method to
	 * detect if the user cancelled the action, in which case the processor
	 * should be {@code reset()}. More fine-grade interrupt detection should be
	 * implemented in the op/filter method.
	 *
	 * <p>
	 * Example usage:
	 * <pre>
	 * <code>
 try {
    // process/filter
    cancelIfInterrupted();

    // save/write result
    cancelIfInterrupted();

    // set processor outputs and provide editor layers
    cancelIfInterrupted();
 } catch (InterruptedException ex) {
    reset(context); // reset processor
 }
 </code>
	 * </pre>
	 *
	 * @throws InterruptedException
	 */
	default void cancelIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}
	}

	/**
	 * Throws an {@code InterruptedException} if the current thread's
	 * interrupted flag is set, or if the given object is {@code null}.
	 * Convenience method to auto-cancel (and clean up/reset) if some result is
	 * {@code null} in addition to the interrupt flag check.
	 *
	 * <p>
	 * Example usage:
	 * <pre>
	 * <code>
 try {
    // process/filter
    result = Filter.filter(context, op, source);
    cancelIfInterrupted(result);

    // save/write result
    cancelIfInterrupted();

    // set processor outputs and provide editor layers
    cancelIfInterrupted();
 } catch (InterruptedException ex) {
    reset(context); // reset processor
 }
 </code>
	 * </pre>
	 *
	 * @param obj some object, or {@code null}.
	 * @throws InterruptedException
	 */
	default void cancelIfInterrupted(Object obj) throws InterruptedException {
		if (obj == null || Thread.currentThread().isInterrupted()) {
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}
	}

}
