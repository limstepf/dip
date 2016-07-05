package ch.unifr.diva.dip.api.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DIP thread pool. This is a fixed thread pool, reusing a fixed number of
 * threads operating off a shared unbounded queue.
 */
public class DipThreadPool {

	private final int poolSize;
	private final ThreadPoolExecutor executor;

	/**
	 * Creates a new thread pool. Uses as many threads as there are available
	 * processors reported by the Java runtime.
	 */
	public DipThreadPool() {
		this(Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Creates a new thread pool.
	 *
	 * @param poolSize the number of threads in the pool.
	 */
	public DipThreadPool(int poolSize) {
		this.poolSize = poolSize;
		// fixed thread pool
		this.executor = new ThreadPoolExecutor(
				this.poolSize,
				this.poolSize,
				0L,
				TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(),
				new DipThreadFactory()
		);
	}

	/**
	 * Shuts down the thread pool/executer service.
	 */
	public void shutdown() {
		try {
			this.executor.shutdown();
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
	private static class DipThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		DipThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "dip-pool-"
					+ poolNumber.getAndIncrement()
					+ "-thread-";
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

			return t;
		}
	}

}
