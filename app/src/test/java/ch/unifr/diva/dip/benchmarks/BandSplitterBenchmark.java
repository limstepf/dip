package ch.unifr.diva.dip.benchmarks;

import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ops.BandExtractOp;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import ch.unifr.diva.dip.api.services.ProcessorBase;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
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
 * Single-threaded, single loop for all bands vs parallel 3x loops.
 */
public class BandSplitterBenchmark {

	@State(Scope.Benchmark)
	public static class Resources {

		public static final DipThreadPool dtp = new DipThreadPool();

		@Param({"512", "701", "1024", "1511", "2048", "3313", "4096"})
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

	public static class Result {

		public BufferedImage band1;
		public BufferedImage band2;
		public BufferedImage band3;

		public Result() {

		}

		public Result(BufferedImage src) {
			this.band1 = createCompatibleDestImage(src);
			this.band2 = createCompatibleDestImage(src);
			this.band3 = createCompatibleDestImage(src);
		}
	}

	public static BufferedImage createCompatibleDestImage(BufferedImage src) {
		if (src instanceof BufferedMatrix) {
			final BufferedMatrix mat = (BufferedMatrix) src;
			return new BufferedMatrix(
					mat.getWidth(),
					mat.getHeight(),
					1,
					mat.getSampleDataType(),
					mat.getInterleave()
			);
		}

		return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Result singleLoopOp(Resources r) {
		Result result = new Result(r.image);
		WritableRaster srcRaster = r.image.getRaster();
		WritableRaster[] raster = new WritableRaster[]{
			result.band1.getRaster(),
			result.band2.getRaster(),
			result.band3.getRaster()
		};
		for (Location pt : new RasterScanner(r.image, true)) {
			final int sample = srcRaster.getSample(pt.col, pt.row, pt.band);
			raster[pt.band].setSample(pt.col, pt.row, 0, sample);
		}
		return result;
	}

	@Benchmark
	@BenchmarkMode({Mode.AverageTime})
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	public Result concurrent3xLoopOp(Resources r) {
		Result result = new Result();
		BandExtractOp op = new BandExtractOp();
		Rectangle tile = ProcessorBase.getOptimalTileSize(r.numThreads, r.image);

		op.setBand(0);
		result.band1 = ProcessorBase.filter(Resources.dtp, op, r.image, null);

		op.setBand(1);
		result.band2 = ProcessorBase.filter(Resources.dtp, op, r.image, null);

		op.setBand(2);
		result.band3 = ProcessorBase.filter(Resources.dtp, op, r.image, null);

		return result;
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(BandSplitterBenchmark.class.getSimpleName())
				.warmupIterations(5)
				.measurementIterations(5)
				.forks(1)
				.build();

		Collection<RunResult> results = new Runner(opt).run();

		BenchmarkUtils.printRunResults(
				results,
				BandSplitterBenchmark.class.getSimpleName()
		);
	}

}
