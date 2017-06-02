package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Image inversion filter. Inverts samples based on the sample domain, or a
 * custom sample range.
 *
 * <pre>I_inverted(x,y) = M - I(x,y)</pre>, where
 * <pre>M</pre> is the maximum value (e.g. 255 for an 8-bit sample).
 */
public class InvertOp extends NullOp implements TileParallelizable {

	// given range [a, b] then x' = b - x + a, so the rangeOffset is a + b, from
	// which we can substract x to get x'
	private double[] rangeOffset;

	/**
	 * Creates a default InvertOp {@literal w.r.t.} the whole sample domain. Can
	 * handle bit and byte samples only.
	 */
	public InvertOp() {
		this.rangeOffset = null;
	}

	/**
	 * Creates an InvertOp {@literal  w.r.t.} a specified range. Can handle
	 * samples in any precision.
	 *
	 * @param min minimum value(s)
	 * @param max maximum value(s)
	 */
	public InvertOp(double[] min, double[] max) {
		this.rangeOffset = new double[min.length];

		for (int i = 0; i < min.length; i++) {
			this.rangeOffset[i] = min[i] + max[i];
		}
	}

	private boolean hasRange() {
		return (this.rangeOffset != null);
	}

	// returns value specified for the band, or the last one defined
	private double get(int index, double[] values) {
		if (index < values.length) {
			return values[index];
		}

		return values[values.length - 1];
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, src.getColorModel());
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		if (hasRange()) {
			for (Location pt : new RasterScanner(src, true)) {
				final int sample = srcRaster.getSample(pt.col, pt.row, pt.band);
				dstRaster.setSample(
						pt.col, pt.row, pt.band,
						get(pt.band, this.rangeOffset) - sample
				);
			}
		} else {
			final int maxValue = ImagingUtils.maxSampleValue(src);

			for (Location pt : new RasterScanner(src, true)) {
				final int sample = srcRaster.getSample(pt.col, pt.row, pt.band);
				dstRaster.setSample(
						pt.col, pt.row, pt.band,
						maxValue - sample
				);
			}
		}

		return dst;
	}

}
