package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.features.Histogram;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 * ConcurrentOp (and DipThreadPool) integration tests.
 */
public class ConcurrentOpIT {

	static DipThreadPool threadPool;

	@BeforeClass
	public static void init() {
		// just test the default setup with numThreads = availableProcessors
		threadPool = new DipThreadPool();
	}

	@AfterClass
	public static void shutdown() {
		threadPool.shutdown();
	}

	/**
	 * Tests that each pixel is written to exactly once, no matter by what
	 * thread.
	 */
	@Test
	public void testEachPixelOnce() {
		List<Integer> tileSizes = Arrays.asList(33, 100, 250);
		List<Integer> widths = Arrays.asList(66, 337, 800);
		List<Integer> heights = Arrays.asList(31, 229, 600);
		BufferedImageOp op = new TestOp();

		for (Integer tile : tileSizes) {
			for (Integer width : widths) {
				for (Integer height : heights) {
					BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
					ConcurrentTileOp cop = new ConcurrentTileOp(op, tile, tile, threadPool);
					BufferedImage out = cop.filter(image, null);
					Histogram histogram = new Histogram(out, 0);
					// image starts out all black, TestOp is supposed to increment all
					// samples by 1. So in our grayscale histogram we should have all
					// pixels in the second bin now
					int pixels = width * height;
					assertEquals(
							"all pixels are in the second bin",
							histogram.getValue(1),
							pixels
					);
				}
			}
		}
	}

	/**
	 * Simple test op that increments each sample by one.
	 */
	public static class TestOp extends NullOp implements TileParallelizable {

		@Override
		public BufferedImage filter(BufferedImage src, BufferedImage dst) {
			if (dst == null) {
				dst = createCompatibleDestImage(src, src.getColorModel());
			}

			final WritableRaster srcRaster = src.getRaster();
			final WritableRaster dstRaster = dst.getRaster();

			for (Location pt : new RasterScanner(src, true)) {
				final int sample = srcRaster.getSample(pt.col, pt.row, pt.band);
				dstRaster.setSample(
						pt.col, pt.row, pt.band,
						sample + 1
				);
			}

			return dst;
		}
	}

}
