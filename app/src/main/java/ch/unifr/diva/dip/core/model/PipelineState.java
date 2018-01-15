package ch.unifr.diva.dip.core.model;

/**
 * The state of a (runnable) pipeline. ...with respect to the page the pipeline
 * is assigned to.
 */
public enum PipelineState {

	/**
	 * The none (or empty) state. There is no pipeline (assigned to the page).
	 * Note that DIP uses an empty pipeline on-the-fly for pages with no
	 * pipeline assigned (just to display the image). In such a case the state
	 * will be {@code READY}, and not {@code NONE} like you might expect.
	 */
	NONE,
	/**
	 * Error state. There exists a processor in the pipeline with processor
	 * state {@code ERROR}, {@code UNAVAILABLE}, or {@code UNCONNECTED}.
	 */
	ERROR,
	/**
	 * Waiting state. All remaining processor in the pipeline with processor
	 * state {@code PROCESSING} can not be automatically processed (i.e.
	 * {p.canProcess() == false}).
	 */
	WAITING,
	/**
	 * Processing state. There exists a processor in the pipeline that can be
	 * processed (automatically).
	 */
	PROCESSING,
	/**
	 * Ready state. All processors in the pipeline are in the processor state
	 * {@code READY}.
	 */
	READY;

	/**
	 * Safely returns a pipeline state by its name.
	 *
	 * @param name name of the visibility mode.
	 * @return the pipeline state with the given name, or the default pipeline
	 * state.
	 */
	public static PipelineState get(String name) {
		try {
			return PipelineState.valueOf(name);
		} catch (IllegalArgumentException ex) {
			return getDefault();
		}
	}

	/**
	 * Returns the default pipeline state.
	 *
	 * @return the default pipeline state.
	 */
	public static PipelineState getDefault() {
		return NONE;
	}

}
