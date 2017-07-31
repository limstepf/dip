package ch.unifr.diva.dip.awt.imaging.rescaling;

import ch.unifr.diva.dip.imaging.rescaling.AbstractFilteredRescaling;
import ch.unifr.diva.dip.imaging.rescaling.FilterFunction;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * General filtered rescaling for AWT BufferedImage.
 */
public class AwtFilteredRescaling extends AbstractFilteredRescaling {

	protected PixelWeight[] tmpY;
	protected BufferedImage dst;

	/**
	 * Creates a new rescaling filter with a box filter.
	 */
	public AwtFilteredRescaling() {
		super();
	}

	/**
	 * Creates a new rescaling filter.
	 *
	 * @param filterf the filter function used for resampling.
	 * @param maxValues max. values of the bands.
	 */
	public AwtFilteredRescaling(FilterFunction filterf, double[] maxValues) {
		super(filterf, maxValues);
	}

	@Override
	protected void invalidate() {
		super.invalidate();

		this.tmpY = new PixelWeight[srcHeight];
		for (int k = 0; k < srcHeight; k++) {
			tmpY[k] = new PixelWeight();
		}
	}

	/**
	 * Rescales the source image. This is a static method for single uses. Make
	 * sure to manually initialize a {@code AwtFilteredRescaling} object for
	 * repeated useage; changes are we don't have do recalculate the pixel
	 * contributions all the time.
	 *
	 * @param filterf the filter function.
	 * @param src the source image.
	 * @param xscale the X scale.
	 * @param yscale the Y scale.
	 * @return the destination image.
	 */
	public static BufferedImage zoom(FilterFunction filterf, BufferedImage src, double xscale, double yscale) {
		final AwtFilteredRescaling r = new AwtFilteredRescaling();
		r.setFiterFunction(filterf);
		return r.zoom(src, xscale, yscale);
	}

	/**
	 * Rescales the source image. This is a static method for single uses. Make
	 * sure to manually initialize a {@code AwtFilteredRescaling} object for
	 * repeated useage; changes are we don't have do recalculate the pixel
	 * contributions all the time.
	 *
	 * @param filterf the filter function.
	 * @param src the source image.
	 * @param dst the destination image. Must not be {@code null}.
	 * @return the destination image.
	 */
	public static BufferedImage zoom(FilterFunction filterf, BufferedImage src, BufferedImage dst) {
		final AwtFilteredRescaling r = new AwtFilteredRescaling();
		r.setFiterFunction(filterf);
		return r.zoom(src, dst);
	}

	/**
	 * Rescales the source image.
	 *
	 * @param src the source image.
	 * @param xscale the X scale.
	 * @param yscale the Y scale.
	 * @return the destination image.
	 */
	public BufferedImage zoom(BufferedImage src, double xscale, double yscale) {
		final int width = (int) Math.round(src.getWidth() * xscale);
		final int height = (int) Math.round(src.getHeight() * yscale);
		if (dst == null || this.dstWidth != width || this.dstHeight != height) {
			this.dst = AwtRescaling.createCompatibleDestImage(src, width, height);
			this.dstWidth = width;
			this.dstHeight = height;
			this.numBands = src.getRaster().getNumBands();
			this.X = null; // must be invalidated
		}

		return zoom(src);
	}

	/**
	 * Rescales the source image.
	 *
	 * @param src the source image.
	 * @param dst the destination image. Must not be {@code null}.
	 * @return the destination image.
	 */
	public BufferedImage zoom(BufferedImage src, BufferedImage dst) {
		this.dst = dst;
		final int width = dst.getWidth();
		final int height = dst.getHeight();
		if (this.dstWidth != width || this.dstHeight != height) {
			this.dstWidth = width;
			this.dstHeight = height;
			this.numBands = src.getRaster().getNumBands();
			this.X = null; // must be invalidated
		}

		return zoom(src);
	}

	/**
	 * Rescales the source image.
	 *
	 * @param src the source image.
	 * @return the destination image.
	 */
	public BufferedImage zoom(BufferedImage src) {
		if (mustBeInvalidated(src.getWidth(), src.getHeight())) {
			invalidate(src.getWidth(), src.getHeight(), dst.getWidth(), dst.getHeight());
		}

		final int[] srcData = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
		final int[] dstData = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();

		for (int xx = 0; xx < dstWidth; xx++) {
			final PixelContributions contribX = X[xx];

			for (int k = 0; k < srcHeight; k++) {

				final int[][] pixels = new int[contribX.n][4];
				for (int j = 0; j < contribX.n; j++) {
					pixels[j] = unpack(srcData[index(contribX.contributions[j].pixel, k, srcWidth)]);
				}

				for (int band = 0; band < numBands; band++) {
					boolean bPelDelta = false;
					final double pel = pixels[0][band];
					tmpY[k].rgba[band] = 0; // reset weight

					for (int j = 0; j < contribX.n; j++) {
						final double pel2 = (j == 0) ? pel : pixels[j][band];
						if (pel2 != pel) {
							bPelDelta = true;
						}
						tmpY[k].add(band, pel2 * contribX.contributions[j].weight);

					}
					tmpY[k].set(band, pel, bPelDelta, maxValues[band]);
				}
			}

			for (int i = 0; i < dstHeight; i++) {
				final PixelWeight weight = new PixelWeight();
				for (int band = 0; band < numBands; band++) {
					boolean bPelDelta = false;
					final double pel = tmpY[Y[i].contributions[0].pixel].rgba[band];
					for (int j = 0; j < Y[i].n; j++) {
						final double pel2 = (j == 0) ? pel : tmpY[Y[i].contributions[j].pixel].rgba[band];
						if (pel2 != pel) {
							bPelDelta = true;
						}
						weight.add(band, pel2 * Y[i].contributions[j].weight);
					}
					weight.set(band, pel, bPelDelta, maxValues[band]);
				}

				dstData[index(xx, i, dstWidth)] = weight.getPackedInt();
			}
		}

		return dst;
	}

}
