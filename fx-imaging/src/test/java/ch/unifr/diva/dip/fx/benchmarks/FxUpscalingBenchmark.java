package ch.unifr.diva.dip.fx.benchmarks;

import ch.unifr.diva.dip.imaging.rescaling.FxRescaling;
import ch.unifr.diva.dip.imaging.utils.AdaptiveIntBuffer;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javafx.scene.image.WritableImage;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JavaFX nearest-neighbor (NN) upscaling/resampling benchmark. Benchmarking the
 * bresenham resampling algorithm for JavaFX images.
 */
public class FxUpscalingBenchmark {

	@State(Scope.Benchmark)
	public static class Resources {

		@Param({"2", "4", "8", "16", "32", "64"})
//		@Param({"1.5", "2", "4"})
		double scale;

		WritableImage image;
		WritableImage dst;

		@Setup
		public void setup() {
			image = BenchmarkUtils.newRandomFxImage(100);
//			image = BenchmarkUtils.newRandomFxImage(960, 540);
			int w = FxRescaling.scaledWidth(image, scale);
			int h = FxRescaling.scaledHeight(image, scale);
			dst = new WritableImage(w, h);
		}
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage bresenhamFx(Resources r) {
		return FxRescaling.bresenham(
				r.image, null,
				r.scale, r.scale
		);
	}

	public static AdaptiveIntBuffer fxIntBuffer = new AdaptiveIntBuffer(2);

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage bresenhamFxIntBuffer(Resources r) {
		final int width = FxRescaling.scaledWidth(r.image, r.scale);
		final int height = FxRescaling.scaledHeight(r.image, r.scale);

		return FxRescaling.bresenham(
				r.image, null,
				r.scale, r.scale,
				fxIntBuffer.get(0, r.image),
				fxIntBuffer.get(1, width * height)
		);
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage bresenhamFxIntBufferDst(Resources r) {
		final int width = FxRescaling.scaledWidth(r.image, r.scale);
		final int height = FxRescaling.scaledHeight(r.image, r.scale);

		return FxRescaling.bresenham(
				r.image, r.dst,
				fxIntBuffer.get(0, r.image),
				fxIntBuffer.get(1, width * height)
		);
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public WritableImage nativeNNAwt(Resources r) {
		return FxRescaling.zoom(
				r.image, null,
				r.scale, r.scale,
				RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
		);
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(FxUpscalingBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				FxUpscalingBenchmark.class.getSimpleName()
		);
	}

}
