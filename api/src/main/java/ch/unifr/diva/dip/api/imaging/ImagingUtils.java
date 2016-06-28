package ch.unifr.diva.dip.api.imaging;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * Imaging utilities.
 */
public class ImagingUtils {

	private ImagingUtils() {
		// nope :)
	}

	/**
	 * Returns the number of bands used by the color model. Note that some
	 * images (e.g. in the png format, indexed ones, I believe) can return 1 if
	 * asked about {@code src.getRaster().getNumBands()}, although
	 * {@code src.getColorModel().getNumComponents()} returns 3 (as expected).
	 *
	 * @param src the image.
	 * @return the number of bands.
	 */
	public static int numBands(BufferedImage src) {
		return src.getColorModel().getNumComponents();
	}

	/**
	 * Return the maximum number of bands used by the color models of two
	 * images.
	 *
	 * @param src first image.
	 * @param dst second image.
	 * @return maximum number of bands (or components).
	 */
	public static int maxBands(BufferedImage src, BufferedImage dst) {
		return maxBands(src.getColorModel(), dst.getColorModel());
	}

	/**
	 * Return the maximum number of bands used by two color models.
	 *
	 * @param src first color model.
	 * @param dst second color model.
	 * @return maximum number of bands (or components).
	 */
	public static int maxBands(ColorModel src, ColorModel dst) {
		return Math.max(src.getNumComponents(), dst.getNumComponents());
	}

	/**
	 * Returns the maximum sample value. This only works for BufferedImage's
	 * that use the full range of values (binary from 0 to 1, byte from 0 to
	 * 255), but will fail, e.g. for BufferedMatrix (backed by float) and
	 * unknown limited range like -1.0 to 1.0 or what not.
	 *
	 * @param src image source.
	 * @return maximum sample value (e.g. 255 for an 8-bit sample) of the first
	 * band.
	 */
	public static int maxSampleValue(BufferedImage src) {
		return maxSampleValue(src, 0);
	}

	/**
	 * Returns the maximum sample value. This only works for BufferedImage's
	 * that use the full range of values (binary from 0 to 1, byte from 0 to
	 * 255), but will fail, e.g. for BufferedMatrix (backed by float) and
	 * unknown limited range like -1.0 to 1.0 or what not.
	 *
	 * @param src image source.
	 * @param band specified band.
	 * @return maximum sample value (e.g. 255 for an 8-bit sample).
	 */
	public static int maxSampleValue(BufferedImage src, int band) {
		return (int) Math.pow(2, src.getSampleModel().getSampleSize(band)) - 1;
	}

	/**
	 * Clamps a sample to the given integer range.
	 *
	 * @param sample the sample.
	 * @param min minimal value.
	 * @param max maximal value.
	 * @return clamped sample (floor(sample) if not min or max).
	 */
	public static int clamp(double sample, int min, int max) {
		int floor = (int) sample;
		if (floor <= min) {
			return min;
		} else if (floor >= max) {
			return max;
		} else {
			return floor;
		}
	}

	/**
	 * Clamps a sample to the given double range.
	 *
	 * @param sample the sample.
	 * @param min minimal value.
	 * @param max maximal value.
	 * @return clamped sample.
	 */
	public static double clamp(double sample, double min, double max) {
		if (sample <= min) {
			return min;
		} else if (sample >= max) {
			return max;
		} else {
			return sample;
		}
	}

	/**
	 * Clamps a float sample to the given float range.
	 *
	 * @param sample the sample.
	 * @param min minimal value.
	 * @param max maximal value.
	 * @return clamped sample.
	 */
	public static float clamp(float sample, float min, float max) {
		if (sample <= min) {
			return min;
		} else if (sample >= max) {
			return max;
		} else {
			return sample;
		}
	}

	/**
	 * Rescales a value.
	 *
	 * @param value the value to be rescaled.
	 * @param srcMin minimum value of the original/source scale.
	 * @param srcMax maximum value of the original/source scale.
	 * @param dstMin minimum value of the new/destination scale.
	 * @param dstMax maximum value of the new/destination scale.
	 * @return rescaled value.
	 */
	public static double rescale(double value, double srcMin, double srcMax, double dstMin, double dstMax) {
		return (dstMax - dstMin) / (srcMax - srcMin) * (value - srcMax) + dstMax;
	}

	/**
	 * Rescales a value.
	 *
	 * @param value the value to be rescaled.
	 * @param ratio the precomputed ratio (max' - min') / (max - min).
	 * @param srcMax maximum value of the original/source scale.
	 * @param dstMax maximum value of the new/destination scale.
	 * @return rescaled value.
	 */
	public static double rescale(double value, double ratio, double srcMax, double dstMax) {
		return ratio * (value - srcMax) + dstMax;
	}
}
