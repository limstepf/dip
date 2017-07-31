package ch.unifr.diva.dip.openimaj.tools.patch;

import java.util.Set;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Patched local contrast filter. The current version (1.3.5) is broken, but
 * should be fixed with the next release.
 *
 * @see
 * <a href="https://github.com/openimaj/openimaj/issues/131">https://github.com/openimaj/openimaj/issues/131</a>
 * @see
 * <a href="https://github.com/openimaj/openimaj/pull/132">https://github.com/openimaj/openimaj/pull/132</a>
 */
public class LocalContrastFilter implements SinglebandImageProcessor<Float, FImage> {

	private Set<Pixel> support;

	/**
	 * Creates a new local contrast filter.
	 *
	 * @param support the support coordinates.
	 */
	public LocalContrastFilter(Set<Pixel> support) {
		this.support = support;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmpImage = new FImage(image.width, image.height);
		float min;
		float max;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				min = Float.MAX_VALUE;
				max = -Float.MAX_VALUE;
				for (final Pixel sp : support) {
					final int xx = x + sp.x;
					final int yy = y + sp.y;

					if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
						min = Math.min(min, image.pixels[yy][xx]);
						max = Math.max(max, image.pixels[yy][xx]);
					}
				}
				tmpImage.pixels[y][x] = max - min;
			}
		}
		image.internalAssign(tmpImage);
	}

}
