package ch.unifr.diva.dip.openimaj.utils;

import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.api.datastructures.Floats2D;
import ch.unifr.diva.dip.api.datastructures.MultiFloats2D;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * OpenIMAJUtils unit tests.
 */
public class OpenIMAJUtilsTest {

	public final static float ASSERT_DELTA = 0.0001f;

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testBinaryBufferedImage() throws IOException {
		BufferedImage image = newSingleBandImage(
				BufferedImage.TYPE_BYTE_BINARY,
				3,
				newSamples(64, 1.0f)
		);
		FImage fimage = OpenIMAJUtils.toFImage(image, 0);
		assertEqualSamples(image, fimage);

		BufferedImage image2 = OpenIMAJUtils.toBinaryBufferedImage(fimage);
		assertEqualSamples(image, image2);

		Path file = parent.newFile().toPath();
		OpenIMAJUtils.writeFImage(fimage, file);
		FImage ret = OpenIMAJUtils.readFImage(file);
		assertEqualFImage(fimage, ret);
	}

	@Test
	public void testGrayBufferedImage() throws IOException {
		BufferedImage image = newSingleBandImage(
				BufferedImage.TYPE_BYTE_GRAY,
				3,
				newSamples(12, 255.0f)
		);
		FImage fimage = OpenIMAJUtils.toFImage(image, 0);
		assertEqualSamples(image, fimage);

		BufferedImage image2 = OpenIMAJUtils.toBufferedImage(fimage);
		assertEqualSamples(image, image2);

		Path file = parent.newFile().toPath();
		OpenIMAJUtils.writeFImage(fimage, file);
		FImage ret = OpenIMAJUtils.readFImage(file);
		assertEqualFImage(fimage, ret);
	}

	@Test
	public void testRGBBufferedImage() throws IOException {
		BufferedImage image = newMultiBandImage(BufferedImage.TYPE_INT_RGB, 5, 7);
		MBFImage mbfimage = OpenIMAJUtils.toMBFImage(image);

		int bands = mbfimage.numBands();
		for (int b = 0; b < bands; b++) {
			FImage fimage = mbfimage.bands.get(b);
			assertEqualSamples(getSamples(image, b), fimage.pixels);
		}

		BufferedImage image2 = OpenIMAJUtils.toBufferedImage(
				mbfimage,
				BufferedImage.TYPE_INT_RGB
		);
		for (int b = 0; b < bands; b++) {
			assertEqualSamples(getSamples(image, b), getSamples(image2, b));
		}

		Path file = parent.newFile().toPath();
		OpenIMAJUtils.writeMBFImage(mbfimage, file);
		MBFImage ret = OpenIMAJUtils.readMBFImage(file);
		assertEqualMBFImage(mbfimage, ret);
	}

	@Test
	public void testSingleBandBufferedMatrix() throws IOException {
		BufferedMatrix mat = newSingleBandMatrix(
				4,
				newSamples(32, 4096.0f)
		);
		FImage fimage = OpenIMAJUtils.toFImage(mat, 0);
		assertEqualSamples(mat, fimage);

		BufferedMatrix mat2 = OpenIMAJUtils.toBufferedMatrix(fimage);
		assertEqualSamples(mat, mat2);

		Path file = parent.newFile().toPath();
		OpenIMAJUtils.writeFImage(fimage, file);
		FImage ret = OpenIMAJUtils.readFImage(file);
		assertEqualFImage(fimage, ret);
	}

	@Test
	public void testMultiBandBufferedMatrix() throws IOException {
		BufferedMatrix mat = newMultiBandMatrix(7, 4);
		MBFImage mbfimage = OpenIMAJUtils.toMBFImage(mat);

		int bands = mbfimage.numBands();
		for (int b = 0; b < bands; b++) {
			FImage fimage = mbfimage.bands.get(b);
			assertEqualSamples(getSamples(mat, b), fimage.pixels);
		}

		BufferedMatrix mat2 = OpenIMAJUtils.toBufferedMatrix(mbfimage);
		for (int b = 0; b < bands; b++) {
			assertEqualSamples(getSamples(mat, b), getSamples(mat2, b));
		}

		Path file = parent.newFile().toPath();
		OpenIMAJUtils.writeMBFImage(mbfimage, file);
		MBFImage ret = OpenIMAJUtils.readMBFImage(file);
		assertEqualMBFImage(mbfimage, ret);
	}

	@Test
	public void testToFloats2D() {
		FImage fimage = new FImage(newSamples(7, 12, 255));

		Floats2D floats = OpenIMAJUtils.toFloats2D(fimage, false);
		assertSameArrays(fimage.pixels, floats.data);

		FImage fret = OpenIMAJUtils.toFImage(floats, false);
		assertSameArrays(floats.data, fret.pixels);
		assertSameArrays(fimage.pixels, fret.pixels);

		Floats2D copy = OpenIMAJUtils.toFloats2D(fimage, true);
		assertNotSameArrays(fimage.pixels, copy.data);
		assertEqualSamples(fimage.pixels, copy.data);

		FImage copyret = OpenIMAJUtils.toFImage(copy, true);
		assertNotSameArrays(copy.data, copyret.pixels);
		assertNotSameArrays(fimage.pixels, copyret.pixels);
		assertEqualSamples(fimage.pixels, copyret.pixels);
	}

	@Test
	public void testToMultiFloats2D() {
		FImage[] bands = new FImage[]{
			new FImage(newSamples(11, 6, 255)),
			new FImage(newSamples(11, 6, 255)),
			new FImage(newSamples(11, 6, 255))
		};
		MBFImage mbfimage = new MBFImage(bands);

		MultiFloats2D mfloats = OpenIMAJUtils.toMultiFloats2D(mbfimage, false);
		assertSameArrays(mbfimage, mfloats);

		MBFImage mbfret = OpenIMAJUtils.toMBFImage(mfloats, false);
		assertSameArrays(mbfret, mfloats);
		assertSameArrays(mbfimage, mbfret);

		MultiFloats2D copy = OpenIMAJUtils.toMultiFloats2D(mbfimage, true);
		assertNotSameArrays(mbfimage, copy);
		assertEqualSamples(mbfimage, copy);

		MBFImage copyret = OpenIMAJUtils.toMBFImage(copy, true);
		assertNotSameArrays(copyret, copy);
		assertNotSameArrays(mbfimage, copy);
		assertEqualSamples(mbfimage, copyret);
	}

	public static void assertEqualFImage(FImage A, FImage B) {
		assertEquals("equal width", A.width, B.width);
		assertEquals("equal height", A.height, B.height);
		assertEqualSamples(A.pixels, B.pixels);
	}

	public static void assertEqualMBFImage(MBFImage A, MBFImage B) {
		assertEquals("equal number of bands", A.numBands(), B.numBands());
		for (int b = 0; b < A.numBands(); b++) {
			final FImage f1 = A.bands.get(b);
			final FImage f2 = B.bands.get(b);
			assertEquals("equal width", f1.width, f2.width);
			assertEquals("equal height", f1.height, f2.height);
			assertEqualSamples(f1.pixels, f2.pixels);
		}
	}

	public static void assertSameArrays(MultiFloats2D A, MultiFloats2D B) {
		for (int i = 0; i < A.size(); i++) {
			assertSameArrays(A.get(i).data, B.get(i).data);
		}
	}

	public static void assertNotSameArrays(MultiFloats2D A, MultiFloats2D B) {
		for (int i = 0; i < A.size(); i++) {
			assertNotSameArrays(A.get(i).data, B.get(i).data);
		}
	}

	public static void assertEqualSamples(MultiFloats2D A, MultiFloats2D B) {
		for (int i = 0; i < A.size(); i++) {
			assertEqualSamples(A.get(i).data, B.get(i).data);
		}
	}

	public static void assertSameArrays(MBFImage A, MultiFloats2D B) {
		for (int i = 0; i < A.bands.size(); i++) {
			assertSameArrays(A.bands.get(i).pixels, B.get(i).data);
		}
	}

	public static void assertNotSameArrays(MBFImage A, MultiFloats2D B) {
		for (int i = 0; i < A.bands.size(); i++) {
			assertNotSameArrays(A.bands.get(i).pixels, B.get(i).data);
		}
	}

	public static void assertEqualSamples(MBFImage A, MultiFloats2D B) {
		for (int i = 0; i < A.bands.size(); i++) {
			assertEqualSamples(A.bands.get(i).pixels, B.get(i).data);
		}
	}

	public static void assertSameArrays(MBFImage A, MBFImage B) {
		for (int i = 0; i < A.bands.size(); i++) {
			assertSameArrays(A.bands.get(i).pixels, B.bands.get(i).pixels);
		}
	}

	public static void assertNotSameArrays(MBFImage A, MBFImage B) {
		for (int i = 0; i < A.bands.size(); i++) {
			assertNotSameArrays(A.bands.get(i).pixels, B.bands.get(i).pixels);
		}
	}

	public static void assertEqualSamples(MBFImage A, MBFImage B) {
		for (int i = 0; i < A.bands.size(); i++) {
			assertEqualSamples(A.bands.get(i).pixels, B.bands.get(i).pixels);
		}
	}

	public static void assertSameArrays(float[][] A, float[][] B) {
		assertSame("exact same array", A, B);
		for (int y = 0; y < A.length; y++) {
			assertSame("exact same sub-array", A[y], B[y]);
		}
	}

	public static void assertNotSameArrays(float[][] A, float[][] B) {
		assertNotSame("distinct array", A, B);
		for (int y = 0; y < A.length; y++) {
			assertNotSame("distinct sub-array", A[y], B[y]);
		}
	}

	public static <T extends BufferedImage> void assertEqualSamples(T left, T right) {
		assertEqualSamples(getSamples(left), getSamples(right));
	}

	public static <T extends BufferedImage> void assertEqualSamples(T image, FImage fimage) {
		assertEqualSamples(getSamples(image), fimage.pixels);
	}

	public static void assertEqualSamples(float[] A, float[] B) {
		assertArrayEquals("equal sample values", A, B, ASSERT_DELTA);
	}

	public static void assertEqualSamples(float[][] A, float[][] B) {
		assertEquals("equal number of rows", A.length, B.length);
		for (int y = 0; y < A.length; y++) {
			assertEqualSamples(A[y], B[y]);
		}
	}

	public static void assertEqualSamples(float[] A, float[][] B) {
		int height = B.length;
		int width = B[0].length;
		assertEquals("equal number of samples", A.length, height * width);
		int idx;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				idx = y * width + x;
				assertEquals("equal sample value", A[idx], B[y][x], ASSERT_DELTA);
			}
		}
	}

	public static BufferedMatrix newSingleBandMatrix(int width, float[] data) {
		int height = data.length / width;
		BufferedMatrix mat = new BufferedMatrix(
				width,
				height,
				1,
				BufferedMatrix.DataType.FLOAT,
				BufferedMatrix.Interleave.BSQ
		);
		mat.getRaster().setSamples(0, 0, width, height, 0, data);
		return mat;
	}

	public static BufferedImage newSingleBandImage(int type, int width, float[] data) {
		int height = data.length / width;
		BufferedImage image = new BufferedImage(width, height, type);
		image.getRaster().setSamples(0, 0, width, height, 0, data);
		return image;
	}

	public static BufferedImage newMultiBandImage(int type, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, type);
		int length = width * height;
		int[] pixels = new int[length];
		for (int i = 0; i < length; i++) {
			pixels[i] = (int) (Math.random() * Integer.MAX_VALUE) & 0x00ffffff;
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	public static BufferedMatrix newMultiBandMatrix(int width, int height) {
		BufferedMatrix mat = new BufferedMatrix(
				width,
				height,
				3,
				BufferedMatrix.DataType.FLOAT,
				BufferedMatrix.Interleave.BSQ
		);
		WritableRaster raster = mat.getRaster();
		for (Location pt : new RasterScanner(mat, true)) {
			raster.setSample(pt.col, pt.row, pt.band, Math.random() * 255.0f);
		}
		return mat;
	}

	public static float[] newSamples(int n, float max) {
		final float[] samples = new float[n];
		for (int i = 0; i < n; i++) {
			if (max == 1.0f) {
				samples[i] = (Math.random() > .5) ? 0 : 1;
			} else {
				samples[i] = (float) Math.random() * max;
			}
		}
		return samples;
	}

	public static float[][] newSamples(int m, int n, float max) {
		final float[][] samples = new float[m][n];
		for (int i = 0; i < m; i++) {
			samples[i] = newSamples(n, max);
		}
		return samples;
	}

	public static <T extends BufferedImage> float[] getSamples(T image) {
		return getSamples(image, 0);
	}

	public static <T extends BufferedImage> float[] getSamples(T image, int band) {
		float[] data = new float[image.getWidth() * image.getHeight()];
		image.getRaster().getSamples(0, 0, image.getWidth(), image.getHeight(), band, data);
		return data;
	}

	public static <T extends BufferedImage> void printSamples(T image) {
		printSamples(image.getWidth(), getSamples(image));
	}

	public static void printSamples(FImage fimage) {
		printSamples(fimage.pixels);
	}

	public static void printSamples(int width, float[] data) {
		int height = data.length / width;
		int idx;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				idx = y * width + x;
				System.out.print(String.format("%2.1f ", data[idx]));
			}
			System.out.println();
		}
		System.out.println();
	}

	public static void printSamples(float[][] data) {
		int height = data.length;
		int width = data[0].length;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				System.out.print(String.format("%2.1f ", data[y][x]));
			}
			System.out.println();
		}
		System.out.println();
	}

}
