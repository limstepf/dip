package ch.unifr.diva.dip.awt.imaging.ops;

import java.awt.image.BufferedImage;

/**
 * Logical AND-filter. Performs a logical AND for all samples of two images.
 */
public class AndOp extends BinaryImageOp {

	public AndOp(BufferedImage left) {
		super(left);
	}

	@Override
	public int combine(int s1, int s2) {
		return s1 & s2;
	}
}
