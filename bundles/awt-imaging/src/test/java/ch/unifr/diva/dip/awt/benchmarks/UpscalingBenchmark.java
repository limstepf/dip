package ch.unifr.diva.dip.awt.benchmarks;

import ch.unifr.diva.dip.awt.imaging.interpolation.Interpolation;
import ch.unifr.diva.dip.awt.imaging.ops.ResampleOp;
import ch.unifr.diva.dip.awt.imaging.rescaling.AwtFilteredRescaling;
import ch.unifr.diva.dip.awt.imaging.rescaling.AwtRescaling;
import ch.unifr.diva.dip.imaging.rescaling.ResamplingFilter;
import java.awt.RenderingHints;
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
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Resampling benchmark (upscaling). Comparing ResizeOp, FilteredRescaling, and
 * AwtRescaling.
 */
public class UpscalingBenchmark {

	@State(Scope.Benchmark)
	public static class Resources {

		@Param({"2", "4", "8", "16", "32", "64", "128", "256", "512"})
		int scale;

		// 0=NN, 1=BILINEAR, 2=BICUBIC
		@Param({"0", "1", "2"})
		int type;

		BufferedImage image;

		@Setup
		public void setup() {
			image = BenchmarkUtils.newRandomImage(5, BufferedImage.TYPE_INT_RGB);
		}
	}

	static double[] min = new double[]{0, 0, 0};
	static double[] max = new double[]{255, 255, 255};

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage resampleOp(Resources r) {
		return new ResampleOp(
				r.scale, r.scale, getResampleInterpolation(r.type), min, max
		).filter(r.image, null);
	}

	public Interpolation getResampleInterpolation(int type) {
		switch (type) {
			case 1:
				return Interpolation.BILINEAR;
			case 2:
				return Interpolation.BICUBIC;
			default:
				return Interpolation.NEAREST_NEIGHBOR;
		}
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage filteredRescaling(Resources r) {
		return AwtFilteredRescaling.zoom(getRescalingFilter(r.type), r.image, r.scale, r.scale);
	}

	// cached pixel contributions
	public static AwtFilteredRescaling awtfr = new AwtFilteredRescaling();

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage filteredRescalingCached(Resources r) {
		awtfr.setFiterFunction(getRescalingFilter(r.type));
		return awtfr.zoom(r.image, r.scale, r.scale);
	}

	public ResamplingFilter getRescalingFilter(int type) {
		switch (type) {
			case 1:
				return ResamplingFilter.TRIANGLE;
			case 2:
				return ResamplingFilter.CATMULL_ROM;
			default:
				return ResamplingFilter.BOX;
		}
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage awtRescaling(Resources r) {
		return AwtRescaling.zoom(r.image, null, r.scale, r.scale, getRenderingHint(r.type));
	}

	public Object getRenderingHint(int type) {
		switch (type) {
			case 1:
				return RenderingHints.VALUE_INTERPOLATION_BILINEAR;
			case 2:
				return RenderingHints.VALUE_INTERPOLATION_BICUBIC;
			default:
				return RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(UpscalingBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				UpscalingBenchmark.class.getSimpleName()
		);
	}

}
