package ch.unifr.diva.dip.eventbus.events;

/**
 * A processor notification. Processor notifications always apply to the
 * processors in the pipeline of the currently active/selected page of the
 * project.
 */
public class ProcessorNotification {

	/**
	 * Type of the processor notification.
	 */
	public enum Type {

		/**
		 * Notification that a processor has been un-/selected. If
		 * {@code processorId} is negative, then no processor has been selected
		 * (or the lastly selected processor has been unselected).
		 */
		SELECTED;
	}

	/**
	 * The type of the processor notification.
	 */
	public final Type type;

	/**
	 * The processor id in the pipeline of the selected page, or -1.
	 */
	public final int processorId;

	/**
	 * Creates a new processor notification.
	 *
	 * @param type type of the processor notification.
	 */
	public ProcessorNotification(Type type) {
		this(type, -1);
	}

	/**
	 * Creates a new processor notification.
	 *
	 * @param type type of the processor notification.
	 * @param processorId the processor id in the pipeline of the selected page,
	 * or -1.
	 */
	public ProcessorNotification(Type type, int processorId) {
		this.type = type;
		this.processorId = processorId;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "type=" + type.name()
				+ ", processorId=" + processorId
				+ "}";
	}

}
