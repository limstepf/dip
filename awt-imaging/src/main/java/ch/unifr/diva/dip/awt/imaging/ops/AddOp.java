package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import java.awt.image.BufferedImage;

/**
 * Arithmetic add filter. Adds (and clamps if necessary) all samples of two
 * images.
 */
public class AddOp extends BinaryImageOp {

	private final int maxValue;

	public AddOp(BufferedImage left) {
		super(left);

		this.maxValue = ImagingUtils.maxSampleValue(left);
	}

	@Override
	public int combine(int s1, int s2) {
		return ImagingUtils.clamp(
				(s1 + s2) * 0.5,
				0,
				maxValue
		);
	}
}
