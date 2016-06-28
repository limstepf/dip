package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Image inversion filter.
 *
 * <pre>I_inverted(x,y) = M - I(x,y)</pre>, where
 * <pre>M</pre> is the maximum value (e.g. 255 for an 8-bit sample).
 */
public class InvertOp extends NullOp {

	// TODO: make this work with any SimpleColorModel's and respect
	// the defined ranges. Then again, not sure how usefull this is...

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, src.getColorModel());
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		final int maxValue = ImagingUtils.maxSampleValue(src);

		for (Location pt : new RasterScanner(src, true)) {
			final int sample = srcRaster.getSample(pt.col, pt.row, pt.band);
			dstRaster.setSample(pt.col, pt.row, pt.band, maxValue - sample);
		}

		return dst;
	}
}
