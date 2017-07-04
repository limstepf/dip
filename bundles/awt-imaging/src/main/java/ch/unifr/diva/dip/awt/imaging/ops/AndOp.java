package ch.unifr.diva.dip.awt.imaging.ops;

import java.awt.image.BufferedImage;

/**
 * Logical AND-filter. Performs a logical AND for all samples of two images.
 */
public class AndOp extends BinaryImageOp {

	/**
	 * Creates a new, logical AND-filter.
	 *
	 * @param left the left (or first source) image.
	 */
	public AndOp(BufferedImage left) {
		super(left);
	}

	@Override
	public int combine(int s1, int s2) {
		return s1 & s2;
	}
}
