package ch.unifr.diva.dip.core.execution;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.core.model.RunnablePipeline;
import ch.unifr.diva.dip.core.model.RunnableProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parallel pipeline executor. The parallel pipeline executor executes
 * processors as soon as all their required inputs are set and satisfied. There
 * is no limit on how many processors may execute in parallel.
 */
public class ParallelPipelineExecutor extends PipelineExecutor {

	/**
	 * Minimum number of processors/threads needed to use a thread pool.
	 */
	protected static final int POOL_MIN_THREADS = 12;

	protected final int pipelineSize;
	protected final ParallelExecutor executor;
	protected final Map<RunnableProcessor, Future<?>> executionMap;
	protected final Object executionLock;
	protected final Object waitLock;
	private volatile int counter;
	protected volatile boolean interrupted;

	/**
	 * Creates a new parallel pipeline executor.
	 *
	 * @param runnable the runnable pipeline.
	 */
	public ParallelPipelineExecutor(RunnablePipeline runnable) {
		this(runnable, new NullPipelineExecutionLogger());
	}

	/**
	 * Creates a new parallel pipeline executor.
	 *
	 * @param runnable the runnable pipeline.
	 * @param logger the pipeline execution logger.
	 */
	public ParallelPipelineExecutor(RunnablePipeline runnable, PipelineExecutionLogger logger) {
		super(runnable, logger);
		this.pipelineSize = runnable.processors().size();
		if (pipelineSize < POOL_MIN_THREADS) {
			this.executor = new ThreadExecutor();
		} else {
			this.executor = new ThreadPoolExecutor();
		}
		this.executionMap = new HashMap<>();
		this.executionLock = new Object();
		this.waitLock = new Object();
		this.interrupted = false;
	}

	@Override
	public void cancel() {
		synchronized (executionLock) {
			this.interrupted = true;
			// interrupt running threads
			for (Future<?> f : executionMap.values()) {
				f.cancel(true);
			}
			// and release waiting lock right away
			releaseWaitLock();
		}
	}

	@Override
	public boolean isCancelled() {
		return this.interrupted;
	}

	@Override
	protected void doProcess() {
		final List<RunnableProcessor> processing = getProcessing();
		if (processing.isEmpty()) {
			executor.shutdown();
			return;
		}

		for (RunnableProcessor processor : processing) {
			if (interrupted) {
				break;
			}
			processProcessor(processor, 1);
		}

		// wait for all worker threads to arrive
		if (!interrupted) {
			waitForProcessors();
		}
		executor.shutdown();
	}

	protected void waitForProcessors() {
		synchronized (waitLock) {
			try {
				waitLock.wait();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

	protected void releaseWaitLock() {
		synchronized (waitLock) {
			waitLock.notify();
		}
	}

	protected void arriveProcessor(RunnableProcessor processor) {
		synchronized (executionLock) {
			counter--;
			executionMap.remove(processor);
			// last one to finish releases the wait lock
			if (counter == 0) {
				releaseWaitLock();
			}
		}
	}

	protected void processProcessor(RunnableProcessor processor, int pipelineStage) {
		synchronized (executionLock) {
			if (interrupted) {
				return;
			}
			if (executionMap.containsKey(processor)) {
				return;
			}
			counter++;
			executionMap.put(
					processor,
					executor.submit(new ExecutionWrapper(processor, pipelineStage))
			);
		}
	}

	protected void processDependentProcessors(RunnableProcessor processor, int pipelineStage) {
		final Set<RunnableProcessor> processing = processor.getDependentProcessors();
		for (RunnableProcessor p : processing) {
			if (isProcessing(p)) {
				processProcessor(p, pipelineStage);
			}
		}
	}

	/**
	 * Execution wrapper.
	 */
	protected class ExecutionWrapper implements Runnable {

		protected final RunnableProcessor processor;
		protected final int pipelineStage;

		/**
		 * Creates a new execution wrapper.
		 *
		 * @param processor the runnable processor.
		 * @param pipelineStage the pipeline stage.
		 */
		public ExecutionWrapper(RunnableProcessor processor, int pipelineStage) {
			this.processor = processor;
			this.pipelineStage = pipelineStage;
		}

		@Override
		public void run() {
			logger.onStartProcessor(processor, pipelineStage);
			processor.process();
			logger.onStopProcessor(processor);

			processDependentProcessors(processor, pipelineStage + 1);
			arriveProcessor(processor);
		}

	}

	/**
	 * Parallel executor.
	 */
	protected interface ParallelExecutor {

		/**
		 * Submits a task to be executed.
		 *
		 * @param task the task.
		 * @return the future.
		 */
		public Future<?> submit(Runnable task);

		/**
		 * Shuts down the parallel executor. May be a null op.
		 */
		default void shutdown() {
		}

	}

	/**
	 * Thread executor. Executes the tasks on a new thread each.
	 */
	protected static class ThreadExecutor implements ParallelExecutor {

		private final static AtomicInteger threadNumber = new AtomicInteger(1);

		@Override
		public Future<?> submit(Runnable task) {
			final FutureTask<Void> future = new FutureTask<>(task, null);
			final Thread thread = new Thread(future);
			thread.setName(
					"dip-parallel-pipeline-executor-thread-"
					+ threadNumber.getAndIncrement()
			);
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.setDaemon(false);
			thread.start();
			return future;
		}

	}

	/**
	 * Thread pool executor. Executes the tasks using a thread pool.
	 */
	protected static class ThreadPoolExecutor implements ParallelExecutor {

		protected final DipThreadPool threadPool = new DipThreadPool(
				"dip-parallel-pipeline-executor-threadpool"
		);

		@Override
		public Future<?> submit(Runnable task) {
			return threadPool.getExecutorService().submit(task);
		}

		@Override
		public void shutdown() {
			threadPool.stop();
		}

	}

}
