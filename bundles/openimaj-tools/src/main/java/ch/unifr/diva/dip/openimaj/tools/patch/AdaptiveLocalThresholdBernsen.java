package ch.unifr.diva.dip.openimaj.tools.patch;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.FilterSupport;
import org.openimaj.image.processing.convolution.AverageBoxFilter;
import org.openimaj.image.processing.threshold.AbstractLocalThreshold;

/**
 * Patched Bernsen's adaptive local thresholding. The current version (1.3.5) is
 * indirectly broken, since {@code LocalContrastFilter} returns a zeroed image,
 * no matter what. Can be removed as soon as the latter is fixed in the next
 * release.
 *
 * @see
 * <a href="https://github.com/openimaj/openimaj/issues/131">https://github.com/openimaj/openimaj/issues/131</a>
 * @see
 * <a href="https://github.com/openimaj/openimaj/pull/132">https://github.com/openimaj/openimaj/pull/132</a>
 */
public class AdaptiveLocalThresholdBernsen extends AbstractLocalThreshold {

	private float threshold;

	/**
	 * Construct the thresholding operator with the given patch size (assumed
	 * square)
	 *
	 * @param threshold the contrast threshold
	 * @param size size of the local image patch
	 */
	public AdaptiveLocalThresholdBernsen(float threshold, int size) {
		super(size);
		this.threshold = threshold;
	}

	/**
	 * Construct the thresholding operator with the given patch size
	 *
	 * @param threshold the contrast threshold
	 * @param size_x width of patch
	 * @param size_y height of patch
	 */
	public AdaptiveLocalThresholdBernsen(float threshold, int size_x, int size_y) {
		super(size_x, size_y);
		this.threshold = threshold;
	}

	@Override
	public void processImage(FImage image) {
		final FImage contrast = image.process(new LocalContrastFilter(FilterSupport.createBlockSupport(sizeX, sizeY)));
		final FImage avg = image.process(new AverageBoxFilter(sizeX, sizeY));

		final float[][] cpix = contrast.pixels;
		final float[][] mpix = avg.pixels;
		final float[][] ipix = image.pixels;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				if (cpix[y][x] < threshold) {
					ipix[y][x] = (mpix[y][x] >= 128) ? 1 : 0;
				} else {
					ipix[y][x] = (ipix[y][x] >= mpix[y][x]) ? 1 : 0;
				}
			}
		}
	}

}
