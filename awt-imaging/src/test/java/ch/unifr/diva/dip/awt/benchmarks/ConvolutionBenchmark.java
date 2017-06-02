package ch.unifr.diva.dip.awt.benchmarks;

import ch.unifr.diva.dip.api.datastructures.FloatKernel;
import ch.unifr.diva.dip.api.datastructures.FloatMatrix;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.awt.imaging.Filter;
import ch.unifr.diva.dip.awt.imaging.ops.ConvolutionOp;
import ch.unifr.diva.dip.awt.imaging.ops.NullOp.SamplePrecision;
import ch.unifr.diva.dip.awt.imaging.padders.ImagePadder;
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
 * Single-pass (kernel) {@literal  vs.} double-pass (row and column vectors)
 * convolution.
 *
 * <p>
 * A 1x3 * 3x1 double-pass is actually slower than a 3x3 single-pass. Probably
 * due to the creation time of the temporary buffered image. One would think we
 * could do without that, but that wouldn't work with our (padded) tiling
 * approach to parallelism (neighbour pixels could already be convolved by the
 * second pass - or not we wouldn't know...). <br />
 *
 * What about a larger kernel though?
 */
public class ConvolutionBenchmark {

	public static final FloatKernel kernel = new FloatKernel(new FloatMatrix(new float[][]{
		{1, 2, 1},
		{2, 4, 2},
		{1, 2, 1}
	}).multiply(1 / 16.0f));

	public static final FloatKernel rowVector = new FloatKernel(new FloatMatrix(new float[][]{
		{0.25f, 0.5f, 0.25f}
	}));

	public static final FloatKernel columnVector = new FloatKernel(new FloatMatrix(new float[][]{
		{0.25f},
		{0.5f},
		{0.25f}
	}));

	public static final ImagePadder padder = ImagePadder.Type.REFLECTIVE.getInstance();

	public static double[] gain = new double[]{1, 1, 1};
	public static double[] bias = new double[]{0, 0, 0};
	public static double[] min = new double[]{0, 0, 0};
	public static double[] max = new double[]{255, 255, 255};

	public static final SamplePrecision precision = SamplePrecision.BYTE;

	@State(Scope.Benchmark)
	public static class Resources {

		public static final DipThreadPool dtp = new DipThreadPool();

		@Param({"512", "701", "1024", "1511", "2048", "3313", "4096", "6667", "8192"})
		int size;

		@Param({"8"})
		public int numThreads;

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
	public BufferedImage onePassConvolution(Resources r) {
		final ConvolutionOp op = new ConvolutionOp(
				kernel, null, padder,
				null, gain, bias, min, max, precision
		);
		return Filter.filter(Resources.dtp, op, r.image, null);
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public BufferedImage twoPassConvolution(Resources r) {
		ConvolutionOp op = new ConvolutionOp(
				columnVector, null, padder,
				null, gain, bias, min, max, precision
		);
		BufferedImage tmp = Filter.filter(Resources.dtp, op, r.image, null);
		op = new ConvolutionOp(
				rowVector, null, padder,
				null, gain, bias, min, max, precision
		);
		return Filter.filter(Resources.dtp, op, tmp, null);
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(ConvolutionBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				ConvolutionBenchmark.class.getSimpleName()
		);
	}

}
