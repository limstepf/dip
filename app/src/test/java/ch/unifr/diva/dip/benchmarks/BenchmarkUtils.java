package ch.unifr.diva.dip.benchmarks;

import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;

/**
 * Benchmark utilities.
 */
public class BenchmarkUtils {

	private BenchmarkUtils() {
		// nope.
	}

	/**
	 * All benchmark results are written to the user's home directory.
	 */
	public static final Path home = Paths.get(System.getProperty("user.home"));

	/**
	 * Writes the run results of a benchmark in CSV format.
	 *
	 * @param results the benchmark run results.
	 * @param filename filename of the file to write to the user's home
	 * directory. Timestamp and file extension will be appended automatically.
	 */
	public static void printRunResults(Collection<RunResult> results, String filename) {
		printRunResults(results, ResultFormatType.CSV, filename);
	}

	/**
	 * Writes the run results of a benchmark.
	 *
	 * @param results the benchmark run results.
	 * @param type the result format type.
	 * @param filename filename of the file to write to the user's home
	 * directory. Timestamp and file extension will be appended automatically.
	 */
	public static void printRunResults(Collection<RunResult> results, ResultFormatType type, String filename) {
		final Path file = home.resolve(
				filename
				+ "-"
				+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
				+ "."
				+ type.name().toLowerCase()
		);
		ResultFormatFactory.getInstance(
				type,
				file.toString()
		).writeOut(results);
	}

	/**
	 * Creates a new BufferedImage with randomly initialized samples.
	 *
	 * @param size width and height of the image.
	 * @param type type of the image.
	 * @return a random image.
	 */
	public static BufferedImage newRandomImage(int size, int type) {
		return newRandomImage(size, size, type);
	}

	/**
	 * Creates a new BufferedImage with randomly initialized samples.
	 *
	 * @param width width of the image.
	 * @param height height of the image.
	 * @param type type of the image.
	 * @return a random image.
	 */
	public static BufferedImage newRandomImage(int width, int height, int type) {
		final BufferedImage image = new BufferedImage(width, height, type);
		final WritableRaster raster = image.getRaster();
		for (Location pt : new RasterScanner(image, true)) {
			raster.setSample(pt.col, pt.row, pt.band, (int) (Math.random() * 255));
		}
		return image;
	}

}
