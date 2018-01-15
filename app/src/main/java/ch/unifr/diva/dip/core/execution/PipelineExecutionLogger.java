package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Pipeline execution logger. Implementations need to be thread safe.
 */
public interface PipelineExecutionLogger {

	/**
	 * Availabble pipeline loggers.
	 */
	public enum Type {

		/**
		 * Null/empty pipeline logger.
		 */
		NULL() {
					@Override
					public PipelineExecutionLogger newInstance() {
						return new NullPipelineExecutionLogger();
					}
				},
		/**
		 * Timing pipeline execution logger.
		 */
		TIMING() {
					@Override
					public PipelineExecutionLogger newInstance() {
						return new TimingPipelineExecutionLogger();
					}
				},
		/**
		 * Printing pipeline execution logger.
		 */
		PRINTING() {
					@Override
					public PipelineExecutionLogger newInstance() {
						return new PrintingPipelineExecutionLogger();
					}
				};

		/**
		 * Creates a new instance of the pipeline execution logger.
		 *
		 * @return a new instance of the pipeline execution logger.
		 */
		public abstract PipelineExecutionLogger newInstance();

		/**
		 * Return the default pipeline execution logger.
		 *
		 * @return default implementation/factory of a pipeline execution
		 * logger.
		 */
		public static Type getDefault() {
			return TIMING;
		}

		/**
		 * Safely returns a valid pipeline execution logger.
		 *
		 * @param name name of the pipeline execution logger.
		 * @return implementation/factory of a pipeline execution logger.
		 */
		public static Type get(String name) {
			try {
				return Type.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return getDefault();
			}
		}

	}

	/**
	 * Callback method called before starting the execution of a pipeline.
	 *
	 * @param pipeline the pipeline about to be executed.
	 */
	default void onStartPipeline(RunnablePipeline pipeline) {

	}

	/**
	 * Callback method called after the execution of a pipeline.
	 *
	 * @param pipeline the pipeline that just finished execution.
	 */
	default void onStopPipeline(RunnablePipeline pipeline) {

	}

	/**
	 * Callback method called before starting the execution of a processor.
	 *
	 * @param processor the processor about to be executed.
	 * @param pipelineStage the pipeline stage of the processor.
	 */
	default void onStartProcessor(RunnableProcessor processor, int pipelineStage) {

	}

	/**
	 * Callback method called after the execution of a processor.
	 *
	 * @param processor the processor that just finished execution.
	 */
	default void onStopProcessor(RunnableProcessor processor) {

	}

	/**
	 * Callback method called once all pipelines have been executed.
	 */
	default void onStopExecution() {

	}

	/**
	 * Checks whether this logger gathers timing data of pipelines and their
	 * processors.
	 *
	 * @return {@code true} if this logger gathers timing data, {@code false}
	 * otherwise.
	 */
	default boolean hasTimings() {
		return !getPipelineTimingMap().isEmpty();
	}

	/**
	 * Returns the pipeline execution timing map. The map uses page ids as keys
	 * to the pipeline timings. Note that these are not necessarily the pipeline
	 * timings of all pages of the current project, just those that were
	 * actually part of the current execution. Iterate over all pages (who "own"
	 * their own timing data) to get the timing data of all pages/pipelines.
	 *
	 * @return the pipeline execution timing map, or an emtpy map if no timing
	 * data has been gathered.
	 */
	@SuppressWarnings("unchecked")
	default Map<Integer, PipelineTiming> getPipelineTimingMap() {
		return Collections.EMPTY_MAP;
	}

	/**
	 * Returns a sorted (by page id) list of pipeline timings.
	 *
	 * @return a sorted (by page id) list of pipeline timings.
	 */
	default List<PipelineTiming> getPipelineTimings() {
		final List<PipelineTiming> timings = new ArrayList<>(getPipelineTimingMap().values());
		Collections.sort(timings, (PipelineTiming t, PipelineTiming t1) -> {
			return Integer.compare(t.getPageId(), t1.getPageId());
		});
		return timings;
	}

	/**
	 * Returns the timing of a pipeline (of a page).
	 *
	 * @param pageId the page id.
	 * @return the pipeline timing.
	 */
	default PipelineTiming getTiming(int pageId) {
		return getPipelineTimingMap().get(pageId);
	}

}
