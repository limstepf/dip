
package ch.unifr.diva.dip.benchmarks;

import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.ops.ColorConvertOp;
import ch.unifr.diva.dip.api.imaging.ops.ConcurrentOp;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * ConcurrentOp tiles fixed {@literal vs.} dynamic tile size benchmark.
 */
public class ConcurrentOpTilesDynamicBenchmark {
	@State(Scope.Benchmark)
	public static class Resources {

		public static final DipThreadPool dtp = new DipThreadPool();

		@Param({"512", "701", "1024", "1511", "2048", "3313", "4096"})
		int size;

		@Param({"8"})
		public int numThreads;

		@Param({"256"})
		public int tileSize;

		BufferedImage image;

		@Setup
		public void setup() {
			image = BenchmarkUtils.newRandomImage(size, BufferedImage.TYPE_INT_RGB);
		}

		@TearDown
		public void shutdown() {
			dtp.shutdown();
		}
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage staticTileSizeOp(Resources r) {
		ConcurrentOp op = new ConcurrentOp(
				new ColorConvertOp(SimpleColorModel.RGB, SimpleColorModel.Lab),
				r.tileSize, // use static tile size, no matter what
				r.tileSize,
				Resources.dtp,
				r.numThreads
		);
		return op.filter(r.image, null);
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage dynamicTileSizeOp(Resources r) {
		Rectangle tileSize = getOptimalTileSize(r.numThreads, r.image);
		ConcurrentOp op = new ConcurrentOp(
				new ColorConvertOp(SimpleColorModel.RGB, SimpleColorModel.Lab),
				tileSize.width, // use dynamic/optimal(?) tile size
				tileSize.height,
				Resources.dtp,
				r.numThreads
		);
		return op.filter(r.image, null);
	}

	public Rectangle getOptimalTileSize(int numThreads, BufferedImage src) {
		return new Rectangle(
				src.getWidth() / numThreads,
				src.getHeight() / numThreads
		);
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(ConcurrentOpTilesDynamicBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				ConcurrentOpTilesDynamicBenchmark.class.getSimpleName()
		);
	}

}
