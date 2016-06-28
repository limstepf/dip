
package ch.unifr.diva.dip.api.imaging.ops;

import java.awt.image.BufferedImage;

/**
 * Logical OR-filter. Performs a logical OR for all samples of two images.
 */
public class OrOp extends BinaryImageOp {

	public OrOp(BufferedImage left) {
		super(left);
	}

	@Override
	public int combine(int s1, int s2) {
		return s1 | s2;
	}
}
