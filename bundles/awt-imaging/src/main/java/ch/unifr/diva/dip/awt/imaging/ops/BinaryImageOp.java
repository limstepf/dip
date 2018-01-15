package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
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
public abstract class BinaryImageOp extends NullOp implements InverseMappedTileParallelizable {

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
	 * @param combinePixels if set to {@code true} RGB pixels will be extracted
	 * and combined, otherwise each sample is combined individually.
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

		final WritableRaster dstRaster = dst.getRaster();

		// possible tile parallelizable offset (true if dstRaster.getParent() != null)
		final int offsetX = dstRaster.getSampleModelTranslateX();
		final int offsetY = dstRaster.getSampleModelTranslateY();
		final int[] samples = new int[2];

		if (this.combinePixels) {
			for (Location pt : new RasterScanner(dstRaster, false)) {
				readRGB(
						pt.col - offsetX,
						pt.row - offsetY,
						left,
						right,
						samples
				);
				dst.setRGB(
						pt.col,
						pt.row,
						combineRGB(samples[0], samples[1])
				);
			}
		} else {
			final WritableRaster leftRaster = this.left.getRaster();
			final WritableRaster rightRaster = right.getRaster();
			for (Location pt : new RasterScanner(dstRaster, true)) {
				readSamples(
						pt.col - offsetX,
						pt.row - offsetY,
						pt.band,
						leftRaster,
						rightRaster,
						samples
				);
				dstRaster.setSample(
						pt.col,
						pt.row,
						pt.band,
						combineRGB(samples[0], samples[1])
				);
			}
		}

		return dst;
	}

	private void readRGB(int col, int row, BufferedImage left, BufferedImage right, int[] samples) {
		samples[0] = left.getRGB(col, row);
		samples[1] = right.getRGB(col, row);
	}

	private void readSamples(int col, int row, int band, WritableRaster left, WritableRaster right, int[] samples) {
		samples[0] = left.getSample(col, row, band);
		samples[1] = right.getSample(col, row, band);
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
