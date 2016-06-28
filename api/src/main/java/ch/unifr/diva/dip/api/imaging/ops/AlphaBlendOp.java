package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import java.awt.image.BufferedImage;

/**
 * Alpha blend filter for blending two image sources.
 *
 * <pre>s' = alpha * s1 + (1 - alpha) * s2</pre>,
 * where
 * <pre>alpha</pre> is a scalar between 0 and 1.
 */
public class AlphaBlendOp extends BinaryImageOp {

	private final int maxValue;
	private double alpha;

	public AlphaBlendOp(BufferedImage left, double alpha) {
		super(left);

		this.maxValue = ImagingUtils.maxSampleValue(left);
		this.alpha = alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getAlpha() {
		return this.alpha;
	}

	@Override
	public int combine(int s1, int s2) {
		return ImagingUtils.clamp(
				(s1 * this.alpha) + ((1.0 - this.alpha) * s2),
				0,
				maxValue
		);
	}
}
