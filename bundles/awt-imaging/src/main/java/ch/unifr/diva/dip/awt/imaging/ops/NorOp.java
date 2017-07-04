package ch.unifr.diva.dip.awt.imaging.ops;

import java.awt.image.BufferedImage;

/**
 * Logical NOR-filter. Performs a logical NOR for all samples of two images.
 */
public class NorOp extends BinaryImageOp {

	/**
	 * Creates a new, logical NOR-filter.
	 *
	 * @param left the left (or first source) image.
	 */
	public NorOp(BufferedImage left) {
		super(left);
	}

	@Override
	public int combine(int s1, int s2) {
		return (s1 | s2) ^ 0xFFFFFFFF;
	}
	
}
