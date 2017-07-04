package ch.unifr.diva.dip.fx.benchmarks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javafx.scene.image.WritableImage;
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

	public static WritableImage newRandomFxImage(int size) {
		return newRandomFxImage(size, size);
	}

	public static WritableImage newRandomFxImage(int width, int height) {
		final WritableImage image = new WritableImage(width, height);
		final int[] buffer = new int[width * height];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = random255() | (random255() << 8) | (random255() << 16) | (255 << 24);
		}
		return image;
	}

	public static int random255() {
		return (int) (Math.random() * 255);
	}
	
}
