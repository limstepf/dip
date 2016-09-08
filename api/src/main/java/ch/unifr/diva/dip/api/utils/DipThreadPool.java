package ch.unifr.diva.dip.api.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DIP thread pool.
 */
public class DipThreadPool {

	private final int poolSize;
	private final ThreadPoolExecutor executor;

	/**
	 * Create a new discarding thread pool. A "thread pool with a bounded work
	 * queue and a {@code DiscardOldestPolicy}. Given too much work,
	 * older/outdated submissions to the thread pool are quietly discarded. This
	 * still guarantees, that the last (or final) submission is quaranteed to be
	 * executed.
	 *
	 * <p>
	 * We use this to process images for viewports (e.g. a preview) where it
	 * doesn't matter if a few inbetween images are dropped (e.g. while fast
	 * scrolling or zooming), as long as the last and up to date image will show
	 * up. A single worker with a queue of size one will guarantee this. With
	 * more workers in the pool, older submissions might still show up last, so
	 * a clock comparision or something is needed in this case (since we can do
	 * that back on the JavaFX thread, no lock/synchronization is needed).
	 *
	 * <p>
	 * Used like this, this also acts as a throttle, compared to the situation
	 * where we'd just use the applications main thread pool (which we shouldn't
	 * do anyways, unless we'd like to have another talk with the dining
	 * philosophers; keyword: task independence!).
	 *
	 * @param poolName prefix used to name the threads.
	 * @param poolSize the number of threads in the pool.
	 * @param workQueueSize the size of the queue to use for holding tasks
	 * before they are executed.
	 * @return a new discarding thread pool.
	 */
	public static DipThreadPool newDiscardingThreadPool(String poolName, int poolSize, int workQueueSize) {
		return new DipThreadPool(
				poolName,
				poolSize,
				new ArrayBlockingQueue<>(workQueueSize),
				new ThreadPoolExecutor.DiscardOldestPolicy()
		);
	}

	/**
	 * Creates a new thread pool. Uses as many threads as there are available
	 * processors reported by the Java runtime.
	 */
	public DipThreadPool() {
		this(Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Creates a new, bounded thread pool.
	 *
	 * @param poolSize the number of threads in the pool.
	 */
	public DipThreadPool(int poolSize) {
		this(
				"dip-threadpool",
				poolSize,
				new LinkedBlockingQueue<>(),
				null
		);
	}

	/**
	 * Creates a new thread pool.
	 *
	 * @param poolName prefix used to name the threads.
	 * @param poolSize the number of threads in the pool.
	 * @param workQueue the queue to use for holding tasks before they are
	 * executed.
	 * @param handler the handler to use when execution is blocked because the
	 * thread bounds and queue capacities are reached. Can be null (uses the
	 * default rejected execution handler/policy).
	 */
	public DipThreadPool(String poolName, int poolSize, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		this.poolSize = poolSize;
		this.executor = new ThreadPoolExecutor(
				this.poolSize,
				this.poolSize,
				0L,
				TimeUnit.MILLISECONDS,
				workQueue,
				new DipThreadFactory(poolName)
		);

		if (handler != null) {
			this.executor.setRejectedExecutionHandler(handler);
		}
	}

	/**
	 * Shuts down the thread pool/executor service. This is equivalent to a call
	 * to {@code stop()} followed by a {@code waitForStop()}.
	 */
	public void shutdown() {
		stop();
		waitForStop();
	}

	/**
	 * Signals to shut down the thread pool/executor service.
	 */
	public void stop() {
		this.executor.shutdown();
	}

	/**
	 * Waits for the thread pool/executor service to shutdown. Waits for a
	 * maximum of 5 seconds, then just kills threads that still haven't shut
	 * down in time.
	 */
	public void waitForStop() {
		try {
			this.executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			// interrupted
		} finally {
			if (!this.executor.isTerminated()) {
				this.executor.shutdownNow();
			}
		}
	}

	/**
	 * Returns the number of threads in the pool.
	 *
	 * @return the number of threads in the pool.
	 */
	public int poolSize() {
		return this.poolSize;
	}

	/**
	 * Returns the thread pool as executor service.
	 *
	 * @return the thread pool as executor service.
	 */
	public ExecutorService getExecutorService() {
		return this.executor;
	}

	/**
	 * DIP thread factory.
	 */
	public static class DipThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final Thread.UncaughtExceptionHandler handler;

		/**
		 * Creates a new thread factory.
		 *
		 * @param poolName prefix used to name the threads.
		 */
		DipThreadFactory(String poolName) {
			final SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = poolName
					+ "-"
					+ poolNumber.getAndIncrement()
					+ "-thread-";
			handler = new WorkerExceptionHandler();
		}

		@Override
		public Thread newThread(Runnable r) {
			final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

			if (t.isDaemon()) {
				t.setDaemon(false);
			}

			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}

			t.setUncaughtExceptionHandler(handler);

			return t;
		}

	}

	/**
	 * Exception handler for worker threads. This one just rethrows whatever
	 * exception got thrown, repackaged as {@code RuntimeException}.
	 */
	public static class WorkerExceptionHandler implements Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread thread, Throwable t) {
			throw new RuntimeException(t);
		}

	}

}
