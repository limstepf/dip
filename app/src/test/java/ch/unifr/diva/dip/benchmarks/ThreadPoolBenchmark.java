package ch.unifr.diva.dip.benchmarks;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Just curious about the overhead of executing something on the thread pool.
 * It's about 4-5 microseconds on my machine, so that's virtually nothing.
 * Sweet.
 */
public class ThreadPoolBenchmark {

	@State(Scope.Benchmark)
	public static class Resources {

		public static final DipThreadPool dtp = new DipThreadPool();

		@Param({"1", "2", "4", "8", "16", "32"})
		public int repeat;

		@TearDown
		public void shutdown() {
			dtp.shutdown();
		}
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public Double singleThread(Resources r) {
		int sum = 0;
		for (int i = 0; i < r.repeat; i++) {
			sum += Math.random();
		}
		return sum / (double) r.repeat;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.NANOSECONDS)
	public Double threadPool(Resources r) throws InterruptedException, ExecutionException {
		final Callable<Double> callable = () -> {
			int sum = 0;
			for (int i = 0; i < r.repeat; i++) {
				sum += Math.random();
			}
			return sum / (double) r.repeat;
		};
		final Future<Double> future = Resources.dtp.getExecutorService().submit(callable);
		return future.get();
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(ThreadPoolBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				ThreadPoolBenchmark.class.getSimpleName()
		);
	}

}
