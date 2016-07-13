package ch.unifr.diva.dip.api.imaging.padders;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Base class to test image padders.
 */
public class ImagePadderTestBase {

	public final List<BufferedImage> testImages = Arrays.asList(
			getImage(new byte[][]{
				{1}
			}),
			getImage(new byte[][]{
				{1, 2},
				{3, 4}
			})
	);

	public BufferedImage getImage(byte[][] data) {
		final int height = data.length;
		final int width = data[0].length;
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		final WritableRaster raster = image.getRaster();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				raster.setSample(i, j, 0, data[i][j]);
			}
		}
		return image;
	}

	public static class PaddedResult {

		final public byte[][] data;

		public PaddedResult(byte[][] data) {
			this.data = data;
		}
	}

	public void verifyResult(ImagePadder padder, BufferedImage src, PaddedResult expected) {
		final int w = src.getWidth();
		final int h = src.getHeight();

		for (int i = -w; i < w * 2; i++) {
			for (int j = -h; j < h * 2; j++) {
				int sample = padder.getSample(src, i, j, 0);
				assertEquals(expected.data[i + w][j + h], sample);
			}
		}
	}

	public void verifyResults(ImagePadder padder, List<BufferedImage> images, List<PaddedResult> expected) {
		for (int i = 0; i < expected.size(); i++) {
			verifyResult(padder, testImages.get(i), expected.get(i));
		}
	}
}
