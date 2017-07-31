package ch.unifr.diva.dip.openimaj.tools;

import ch.unifr.diva.dip.openimaj.tools.patch.LocalContrastFilter;
import org.junit.Test;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.FilterSupport;
import org.openimaj.image.processing.convolution.FSobel;
import org.openimaj.image.processing.convolution.Laplacian3x3;
import org.openimaj.math.geometry.line.Line2d;

/**
 * OpenIMAJ playground to mess around. These are no proper unit tests, and
 * aren't run automatically.
 */
public class OpenIMAJPlayground {

	@Test
	public void patchedLocalContrastFilterTest() {
		FImage fimage = new FImage(getRandomSamples(9, 9));
		FImage contrast = fimage.process(new LocalContrastFilter(FilterSupport.createBlockSupport(3, 3)));

		System.out.println("samples:");
		printSamples(fimage.pixels);
		System.out.println();

		System.out.println("local contrast:");
		printSamples(contrast.pixels);
	}

	@Test
	public void sobelTest() {
		int width = 64;
		int height = 64;
		float[][] samples = getSamples(width, height);
		vLine(samples, 16, 255.0f, 1.0f);
		vLine(samples, 48, 255.0f, 1.0f);
		vLine(samples, 52, 96.0f, 1.0f);
		hLine(samples, 24, 255.0f, 1.0f);
		FImage fimage = new FImage(samples);
		FSobel sobel = new FSobel();
		sobel.analyseImage(fimage);

		printScanline(sobel.dx.pixels, 10);
		System.out.println();

		printScanline(sobel.dy.pixels, 10);
		System.out.println();
	}

	@Test
	public void laplacianTest() {
		int width = 64;
		int height = 64;
		float[][] samples = getSamples(width, height);
		vLine(samples, 16, 255.0f, 1.0f);
		vLine(samples, 48, 255.0f, 1.0f);
		vLine(samples, 52, 96.0f, 1.0f);
		hLine(samples, 24, 255.0f, 1.0f);
		FImage fimage = new FImage(samples);
		Laplacian3x3 laplacian = new Laplacian3x3();
		laplacian.processImage(fimage);

		printScanline(fimage.pixels, 10);
		System.out.println();
	}

	@Test
	public void cannyHoughTest() {
		int width = 64;
		int height = 64;
		float[][] samples = getSamples(width, height);
		vLine(samples, 16, 255.0f, 1.0f);
		vLine(samples, 48, 255.0f, 1.0f);
		hLine(samples, 24, 255.0f, 1.0f);
		FImage fimage = new FImage(samples);

		printScanline(samples, 10);
		System.out.println();

		float sigma = 1.0f;
		org.openimaj.image.processing.edges.CannyEdgeDetector canny = new org.openimaj.image.processing.edges.CannyEdgeDetector(sigma);

		canny.processImage(fimage); // turns gray [0, 255] into binary [0, 1]

		printScanline(fimage.pixels, 10);
		System.out.println();

		org.openimaj.image.analysis.algorithm.HoughLines hl = new org.openimaj.image.analysis.algorithm.HoughLines();
		fimage.inverse().analyseWith(hl);
		double d = hl.calculatePrevailingAngle();
		System.out.println("prevailingAngle> " + d);

		for (Line2d line : hl.getBestLines(10)) {
			System.out.println(" - line: " + line);
		}
		hl.clearIterator();
	}

	public static void printSamples(float[][] samples) {
		final int height = samples.length;
		for (int y = 0; y < height; y++) {
			printScanline(samples, y);
			System.out.println();
		}
	}

	public static void printScanline(float[][] samples, int y_pos) {
		final int width = samples[0].length;
		for (int x = 0; x < width; x++) {
			System.out.print(String.format("%3.0f ", samples[y_pos][x]));
		}
	}

	public static float[][] getSamples(int height, int width) {
		float[][] samples = new float[height][width];
		for (int y = 0; y < height; y++) {
			samples[y] = new float[width];
		}
		return samples;
	}

	public static float[][] getRandomSamples(int height, int width) {
		float[][] samples = new float[height][width];
		for (int y = 0; y < height; y++) {
			samples[y] = new float[width];
			for (int x = 0; x < width; x++) {
				samples[y][x] = (float) Math.random() * 255.0f;
			}
		}
		return samples;
	}

	public static void vLine(float[][] samples, int x_pos, float value, float probability) {
		final int height = samples.length;
		for (int y = 0; y < height; y++) {
			if (probability > 0.99f || Math.random() < probability) {
				samples[y][x_pos] = value;
			}
		}
	}

	public static void hLine(float[][] samples, int y_pos, float value, float probability) {
		final int width = samples[0].length;
		for (int x = 0; x < width; x++) {
			if (probability > 0.99f || Math.random() < probability) {
				samples[y_pos][x] = value;
			}
		}
	}

}
