package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline executor. Executes a runnable pipeline as far as (automatically)
 * possible.
 *
 * <ol>
 * <li>Take and process the initial set of processors of the runnable pipeline
 * that are ready to be processed right away. This means that a pipeline
 * executor can continue processing a partially processed pipeline.</li>
 * <li>After a processor has been processed, take the set of immediately
 * following (or deppendent) processors, and process the ones that are now ready
 * to be processed.</li>
 * <li>The pipeline execution stops, once no processor can be automatically
 * processed.</li>
 * </ol>
 *
 */
public abstract class PipelineExecutor {

	/**
	 * Available pipeline executors.
	 */
	public enum Type {

		/**
		 * Serial pipeline executor.
		 */
		SERIAL() {
					@Override
					public PipelineExecutor newInstance(RunnablePipeline runnable, PipelineExecutionLogger logger) {
						return new SerialPipelineExecutor(runnable, logger);
					}
				},
		/**
		 * Parallel pipeline executor.
		 */
		PARALLEL() {
					@Override
					public PipelineExecutor newInstance(RunnablePipeline runnable, PipelineExecutionLogger logger) {
						return new ParallelPipelineExecutor(runnable, logger);
					}
				};

		/**
		 * Creates a new instance of the pipeline executor.
		 *
		 * @param runnable the runnable pipeline.
		 * @param logger the pipeline execution logger.
		 * @return a new instance of the pipeline executor.
		 */
		public abstract PipelineExecutor newInstance(RunnablePipeline runnable, PipelineExecutionLogger logger);

		/**
		 * Return the default pipeline executor.
		 *
		 * @return default implementation/factory of a pipeline executor.
		 */
		public static Type getDefault() {
			return PARALLEL;
		}

		/**
		 * Safely returns a valid pipeline executor.
		 *
		 * @param name name of the pipeline executor.
		 * @return implementation/factory of a pipeline executor.
		 */
		public static Type get(String name) {
			try {
				return Type.valueOf(name);
			} catch (IllegalArgumentException ex) {
				return getDefault();
			}
		}

	}

	protected static final Logger log = LoggerFactory.getLogger(PipelineExecutor.class);
	protected final WeakReference<RunnablePipeline> runnable;
	protected final PipelineExecutionLogger logger;
	protected final Thread executionThread;
	private final static AtomicInteger threadNumber = new AtomicInteger(1);

	/**
	 * Creates a new pipeline executor.
	 *
	 * @param runnable the runnale pipeline.
	 * @param logger the pipeline logger.
	 */
	public PipelineExecutor(RunnablePipeline runnable, PipelineExecutionLogger logger) {
		this.runnable = new WeakReference<>(runnable);
		this.logger = logger;
		// run execution on separate thread, s.t. we may cancel the execution
		this.executionThread = new Thread() {
			@Override
			public void run() {
				final RunnablePipeline rp = PipelineExecutor.this.runnable.get();
				if (rp == null) {
					log.warn(
							"Can not execute pipeline. Weak reference to pipeline expired: {}",
							this
					);
					return;
				}
				logger.onStartPipeline(rp);
				doProcess();
				logger.onStopPipeline(rp);
			}
		};
		executionThread.setName(
				"dip-pipeline-executor-thread-"
				+ threadNumber.getAndIncrement()
		);
		executionThread.setPriority(Thread.NORM_PRIORITY);
		executionThread.setDaemon(false);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{pipeline="
				+ runnable.get()
				+ ", logger="
				+ logger.getClass().getSimpleName()
				+ "}";
	}

	/**
	 * Returns the name of the pipeline executor.
	 *
	 * @return the name of the pipeline executor.
	 */
	public String getExecutorName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * Starts the execution of the pipeline. This method returns immediately.
	 */
	public void process() {
		executionThread.start();
	}

	/**
	 * Starts the execution of the pipeline. This method does not return before
	 * the pipeline has been executed.
	 */
	public void processAndWaitForStop() {
		process();
		waitForStop();
	}

	/**
	 * Waits for the execution of the pipeline to stop.
	 */
	public void waitForStop() {
		if (!executionThread.isAlive()) {
			return;
		}
		try {
			executionThread.join();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Cancels the execution of the pipeline.
	 */
	public abstract void cancel();

	/**
	 * Checks whether the execution is cancelled.
	 *
	 * @return {@code true} if the execution is cancelled, {@code false}
	 * otherwise.
	 */
	public abstract boolean isCancelled();

	/**
	 * Processes the pipeline.
	 */
	protected abstract void doProcess();

	/**
	 * Returns the list of {@code Processable} and ready to be processed
	 * processors.
	 *
	 * @return the list of {@code Processable} and ready to be processed
	 * processors.
	 */
	protected List<RunnableProcessor> getProcessing() {
		final RunnablePipeline rp = this.runnable.get();
		final ArrayList<RunnableProcessor> processing = new ArrayList<>();
		if (rp != null) {
			for (RunnableProcessor p : rp.processors()) {
				if (isProcessing(p)) {
					processing.add(p);
				}
			}
		}
		return processing;
	}

	/**
	 * Checks whether the processor is {@code Processable} and ready to be
	 * processed.
	 *
	 * @param p the runnable processor.
	 * @return {@code true} if the processor is {@code Processable} and ready to
	 * be processed, {@code false} otherwise.
	 */
	protected boolean isProcessing(RunnableProcessor p) {
		return Processor.State.PROCESSING.equals(p.getState())
				&& p.serviceObject().canProcess();
	}

}
