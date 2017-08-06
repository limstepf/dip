package ch.unifr.diva.dip.fx.imaging.rescaling;

import ch.unifr.diva.dip.imaging.rescaling.AbstractFilteredRescaling;
import ch.unifr.diva.dip.imaging.rescaling.FilterFunction;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * General filtered rescaling for JavaFX Image.
 */
public class FxFilteredRescaling extends AbstractFilteredRescaling {

	protected PixelWeight[] tmpY;
	protected WritableImage dst;

	/**
	 * Creates a new rescaling filter with a box filter.
	 */
	public FxFilteredRescaling() {
		super();
	}

	/**
	 * Creates a new rescaling filter.
	 *
	 * @param filterf the filter function used for resampling.
	 * @param maxValues max. values of the bands.
	 */
	public FxFilteredRescaling(FilterFunction filterf, double[] maxValues) {
		super(filterf, maxValues);
	}

	@Override
	protected void invalidate() {
		this.tmpY = new PixelWeight[srcHeight];
		for (int k = 0; k < srcHeight; k++) {
			tmpY[k] = new PixelWeight();
		}
		super.invalidate();
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
	public static WritableImage zoom(FilterFunction filterf, Image src, double xscale, double yscale) {
		final FxFilteredRescaling r = new FxFilteredRescaling();
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
	public static WritableImage zoom(FilterFunction filterf, Image src, WritableImage dst) {
		final FxFilteredRescaling r = new FxFilteredRescaling();
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
	public WritableImage zoom(Image src, double xscale, double yscale) {
		final int width = (int) Math.round(src.getWidth() * xscale);
		final int height = (int) Math.round(src.getHeight() * yscale);
		if (dst == null || this.dstWidth != width || this.dstHeight != height) {
			this.dst = FxRescaling.createCompatibleDestImage(src, width, height);
			this.dstWidth = width;
			this.dstHeight = height;
			this.numBands = 4;
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
	public WritableImage zoom(Image src, WritableImage dst) {
		this.dst = dst;
		final int width = (int) dst.getWidth();
		final int height = (int) dst.getHeight();
		if (this.dstWidth != width || this.dstHeight != height) {
			this.dstWidth = width;
			this.dstHeight = height;
			this.numBands = 4;
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
	public WritableImage zoom(Image src) {

		if (mustBeInvalidated(src.getWidth(), src.getHeight())) {
			invalidate(src.getWidth(), src.getHeight(), dst.getWidth(), dst.getHeight());
		}

		PixelReader srcReader = src.getPixelReader();
		PixelWriter dstWriter = dst.getPixelWriter();

		for (int xx = 0; xx < dstWidth; xx++) {
			final PixelContributions contribX = X[xx];

			for (int k = 0; k < srcHeight; k++) {

				final int[][] pixels = new int[contribX.n][4];
				for (int j = 0; j < contribX.n; j++) {
					pixels[j] = unpack(srcReader.getArgb(contribX.contributions[j].pixel, k));
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

				dstWriter.setArgb(xx, i, weight.getPackedInt());
			}
		}

		return dst;
	}

}
