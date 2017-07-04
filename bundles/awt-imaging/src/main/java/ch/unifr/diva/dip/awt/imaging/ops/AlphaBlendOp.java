package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import java.awt.image.BufferedImage;

/**
 * Alpha blend filter for blending two image sources.
 *
 * <pre>s' = alpha * s1 + (1 - alpha) * s2</pre>, where
 * <pre>alpha</pre> is a scalar between 0 and 1.
 */
public class AlphaBlendOp extends BinaryImageOp {

	private final int maxValue;
	private double alpha;

	/**
	 * Creates a new alpha blend filter.
	 *
	 * @param left the left (or first source) image.
	 * @param alpha the alpha value (in {@code [0.0, 1.0]}, where {@code alpha}
	 * is applied to the left, and {@code 1 - alpha} to the right image.
	 */
	public AlphaBlendOp(BufferedImage left, double alpha) {
		super(left);

		this.maxValue = ImagingUtils.maxSampleValue(left);
		this.alpha = alpha;
	}

	/**
	 * Sets the alpha value.
	 *
	 * @param alpha the alpha value (in {@code [0.0, 1.0]}, where {@code alpha}
	 * is applied to the left, and {@code 1 - alpha} to the right image.
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * Returns the alpha value.
	 *
	 * @return the alpha value (in {@code [0.0, 1.0]}.
	 */
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
