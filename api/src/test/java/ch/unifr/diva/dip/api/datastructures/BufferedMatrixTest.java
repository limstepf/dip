package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.utils.BufferedIO;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@code BufferedMatrix} unit tests.
 */
public class BufferedMatrixTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testWriteAndReadbackFloat() throws IOException {
		final int width = 7;
		final int height = 5;
		final float[][] samples = TestUtils.newFloats2DArray(height, width);
		final BufferedMatrix mat = BufferedMatrix.createBufferedMatrix(samples);
		Path file = parent.newFile().toPath();
		BufferedIO.writeMat(mat, file);
		final BufferedMatrix ret = BufferedIO.readMat(file);
		assertEquals("equal width", mat.getWidth(), ret.getWidth());
		assertEquals("equal height", mat.getHeight(), ret.getHeight());
		assertEquals("equal num. bands", mat.getNumBands(), ret.getNumBands());
		assertEquals("equal sample precision", mat.getSampleDataType(), ret.getSampleDataType());
		final float[][] ret_samples = ret.toFloat2D();
		assertArrayEquals("readback sample values are equal", samples, ret_samples);
	}

	@Test
	public void testWriteAndReadbackDouble() throws IOException {
		final int width = 3;
		final int height = 4;
		final double[][] samples = TestUtils.newDoubles2DArray(height, width);
		final BufferedMatrix mat = BufferedMatrix.createBufferedMatrix(samples);
		Path file = parent.newFile().toPath();
		BufferedIO.writeMat(mat, file);
		final BufferedMatrix ret = BufferedIO.readMat(file);
		assertEquals("equal width", mat.getWidth(), ret.getWidth());
		assertEquals("equal height", mat.getHeight(), ret.getHeight());
		assertEquals("equal num. bands", mat.getNumBands(), ret.getNumBands());
		assertEquals("equal sample precision", mat.getSampleDataType(), ret.getSampleDataType());
		final double[][] ret_samples = ret.toDouble2D();
		assertArrayEquals("readback sample values are equal", samples, ret_samples);
	}

	@Test
	public void testFromAndToFloat() {
		final int width = 3;
		final int height = 4;
		final float[][] samples = TestUtils.newFloats2DArray(height, width);
		final BufferedMatrix mat = BufferedMatrix.createBufferedMatrix(samples);
		assertEquals("correct width", mat.getWidth(), width);
		assertEquals("correct height", mat.getHeight(), height);
		assertEquals("correct num. bands", mat.getNumBands(), 1);
		assertEquals("correct sample precision", mat.getSampleDataType(), BufferedMatrix.DataType.FLOAT);

		final float sum = TestUtils.sum(samples);
		final WritableRaster raster = mat.getRaster();
		float msum = 0;
		int idx;
		float sample;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				idx = y * width + x;
				sample = raster.getSampleFloat(x, y, 0);
				assertEquals(
						"same sample value",
						sample,
						samples[y][x],
						TestUtils.FLOAT_DELTA
				);
				msum += sample;
			}
		}
		assertEquals("same sample sum", sum, msum, TestUtils.FLOAT_DELTA);

		final float[][] ret = mat.toFloat2D();
		assertArrayEquals("toFloat2D equals original 2D array", samples, ret);
	}

	@Test
	public void testFromAndToDouble() {
		final int width = 3;
		final int height = 4;
		final double[][] samples = new double[][]{
			TestUtils.newDoubles(width),
			TestUtils.newDoubles(width),
			TestUtils.newDoubles(width),
			TestUtils.newDoubles(width)
		};
		final BufferedMatrix mat = BufferedMatrix.createBufferedMatrix(samples);
		assertEquals("correct width", mat.getWidth(), width);
		assertEquals("correct height", mat.getHeight(), height);
		assertEquals("correct num. bands", mat.getNumBands(), 1);
		assertEquals("correct sample precision", mat.getSampleDataType(), BufferedMatrix.DataType.DOUBLE);

		final double sum = TestUtils.sum(samples);
		final WritableRaster raster = mat.getRaster();
		double msum = 0;
		int idx;
		double sample;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				idx = y * width + x;
				sample = raster.getSampleDouble(x, y, 0);
				assertEquals(
						"same sample value",
						sample,
						samples[y][x],
						TestUtils.DOUBLE_DELTA
				);
				msum += sample;
			}
		}
		assertEquals("same sample sum", sum, msum, TestUtils.DOUBLE_DELTA);

		final double[][] ret = mat.toDouble2D();
		assertArrayEquals("toDouble2D equals original 2D array", samples, ret);
	}

}
