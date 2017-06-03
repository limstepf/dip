package ch.unifr.diva.dip.awt.imaging.ops;

import java.awt.image.BufferedImage;

/**
 * Logical OR-filter. Performs a logical OR for all samples of two images.
 */
public class OrOp extends BinaryImageOp {

	/**
	 * Creates a new, logical OR-filter.
	 *
	 * @param left the left (or first source) image.
	 */
	public OrOp(BufferedImage left) {
		super(left);
	}

	@Override
	public int combine(int s1, int s2) {
		return s1 | s2;
	}

}
