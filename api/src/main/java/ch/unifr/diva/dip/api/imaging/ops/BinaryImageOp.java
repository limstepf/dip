package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * {@code BinaryImageOp} is an abstract base class implementation for arithmetic
 * (or logic) operations with two source images. Since {@code BufferedImageOp}
 * is not designed to handle two (or more) source images, this is done by
 * "currying" (i.e. the transformation of a function with multiple arguments
 * into a function with only a single argument).
 */
public abstract class BinaryImageOp extends NullOp implements TileParallelizable {

	protected final BufferedImage left;
	protected final boolean combinePixels;

	/**
	 * Creates a new binary image operation without combining pixels.
	 *
	 * @param left the first source image.
	 */
	public BinaryImageOp(BufferedImage left) {
		this(left, false);
	}

	/**
	 * Creates a new binary image operation.
	 *
	 * @param left the first source image.
	 * @param combinePixels if set to True RGB pixels will be extracted and
	 * combined, otherwise each sample is combined individually.
	 */
	public BinaryImageOp(BufferedImage left, boolean combinePixels) {
		this.left = left;
		this.combinePixels = combinePixels;
	}

	/**
	 * Combines two source samples in a way known only by the implementing
	 * subclass.
	 *
	 * @param s1 sample from the first (or left) source.
	 * @param s2 sample from the second (or right) source.
	 * @return combined sample value.
	 */
	public abstract int combine(int s1, int s2);

	/**
	 * Combines two RGB pixels.
	 *
	 * @param p1 pixel of the first source image.
	 * @param p2 pixel of the second source image.
	 * @return the combined pixel.
	 */
	public int combineRGB(int p1, int p2) {
		final Color c1 = new Color(p1);
		final Color c2 = new Color(p2);

		return new Color(
				combine(c1.getRed(), c2.getRed()),
				combine(c1.getGreen(), c2.getGreen()),
				combine(c1.getBlue(), c2.getBlue())
		).getRGB();
	}

	@Override
	public BufferedImage filter(BufferedImage right, BufferedImage dst) {
		final Rectangle leftBounds = this.left.getRaster().getBounds();
		final Rectangle rightBounds = right.getRaster().getBounds();
		final Rectangle intersectBounds = leftBounds.intersection(rightBounds);

		if (dst == null) {
			if ((this.left instanceof BufferedMatrix) || (right instanceof BufferedMatrix)) {
				final BufferedMatrix mat = (BufferedMatrix) getImageWithLeastBands(this.left, right);
				dst = createCompatibleDestMatrix(intersectBounds, mat);
			} else {
				final ColorModel cm = getImageWithLeastBands(this.left, right).getColorModel();
				dst = createCompatibleDestImage(intersectBounds, cm);
			}
		}

		if (this.combinePixels) {
			for (Location pt : new RasterScanner(dst, false)) {
				final int s1 = this.left.getRGB(pt.col, pt.row);
				final int s2 = right.getRGB(pt.col, pt.row);
				dst.setRGB(pt.col, pt.row, combineRGB(s1, s2));
			}
		} else {
			final WritableRaster leftRaster = this.left.getRaster();
			final WritableRaster rightRaster = right.getRaster();
			final WritableRaster dstRaster = dst.getRaster();

			for (Location pt : new RasterScanner(dst, true)) {
				final int s1 = leftRaster.getSample(pt.col, pt.row, pt.band);
				final int s2 = rightRaster.getSample(pt.col, pt.row, pt.band);
				dstRaster.setSample(pt.col, pt.row, pt.band, combine(s1, s2));
			}
		}

		return dst;
	}

	private BufferedImage getImageWithLeastBands(BufferedImage... images) {
		BufferedImage image = images[0];
		int numBands = ImagingUtils.numBands(image);
		for (int i = 1; i < images.length; i++) {
			final int n = ImagingUtils.numBands(images[i]);
			if (n < numBands) {
				image = images[i];
				numBands = n;
			}
		}
		return image;
	}

}
