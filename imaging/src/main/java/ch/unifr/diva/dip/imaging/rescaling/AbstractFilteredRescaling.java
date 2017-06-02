package ch.unifr.diva.dip.imaging.rescaling;

/**
 * Base class for general filtered rescaling. Pixel contributions are cached to
 * be reused for all bands and repeated calls (as long as dimensions are kept
 * the same).
 *
 * <p>
 * Based on {@code filter_rcb.c} by Dale Schumacher and Ray Gardener. See:
 * Schumacher, Dale. General Filtered Image Rescaling, in Graphics Gems III
 * (1992).
 *
 * <ul>
 * <li>Java port by David Dupplaw, Sina Samangooei, and Jonathon Hare
 * (OpenIMAJ).</li>
 *
 * <li>With improvements from TwelveMonkeys and ImageMagick (which in-particular
 * fix normalisation problems).</li>
 * </ul>
 *
 * <p>
 * Further changes/notes:
 * <ul>
 * <li>Use of extended border image padding (clamped indexing), instead of
 * reflective indexing.</li>
 *
 * <li>Pixel-shift correction (upscaling small images, e.g. with a BOX filter is
 * very wrong otherwise). Note that in comparison to native AWT bilinear/bicubic
 * resampling, anything other than a box filter seems to slightly offset the
 * image by half a pixel or what not - yet this isn't due to this pixel-shift
 * here! You can easily verify this by setting PIXEL_SHIFT below to 0. So I'm
 * not really sure where that is coming from... Do we need a PIXEL_SHIFT
 * depending on the filter support or something?</li>
 * </ul>
 */
public abstract class AbstractFilteredRescaling {

	/**
	 * Pixel shift to put filters exactly on the middle of a pixel.
	 */
	protected static final double PIXEL_SHIFT = -0.5;

	protected int numBands;
	protected int srcWidth;
	protected int srcHeight;
	protected int dstWidth;
	protected int dstHeight;
	protected double xscale;
	protected double yscale;
	protected double fwidth;
	protected double[] maxValues;
	protected FilterFunction filterf;
	protected PixelContributions[] Y;
	protected PixelContributions[] X;

	/**
	 * Creates a new rescaling filter using a box filter.
	 */
	public AbstractFilteredRescaling() {
		this(ResamplingFilter.BOX, new double[]{255, 255, 255, 255});
	}

	/**
	 * Creates a new rescaling filter.
	 *
	 * @param filterf the filter function used for resampling.
	 * @param maxValues max. values of the bands.
	 */
	public AbstractFilteredRescaling(FilterFunction filterf, double[] maxValues) {
		setFiterFunction(filterf);
		this.maxValues = maxValues;
	}

	/**
	 * Sets/updates the filter function.
	 *
	 * @param filterf the filter function used for resampling.
	 */
	final public void setFiterFunction(FilterFunction filterf) {
		if (this.filterf != null && this.filterf.equals(filterf)) {
			return;
		}
		this.filterf = filterf;
		this.X = null; // must be invalidated
	}

	/**
	 * Checks whether we have to recalculate the pixel contributions, or not.
	 *
	 * @param srcWidth width of the source image.
	 * @param srcHeight height of the source image.
	 * @return True if pixel contributions have to be (re-)calculated, False if
	 * they can be reused.
	 */
	protected boolean mustBeInvalidated(double srcWidth, double srcHeight) {
		return mustBeInvalidated((int) srcWidth, (int) srcHeight);
	}

	/**
	 * Checks whether we have to recalculate the pixel contributions, or not.
	 *
	 * @param srcWidth width of the source image.
	 * @param srcHeight height of the source image.
	 * @return True if pixel contributions have to be (re-)calculated, False if
	 * they can be reused.
	 */
	protected boolean mustBeInvalidated(int srcWidth, int srcHeight) {
		if (X == null) {
			return true;
		}

		if (this.srcWidth != srcWidth || this.srcHeight != srcHeight) {
			return true;
		}

		return false;
	}

	/**
	 * Invalidates cached data. Such as the pixel contributions, the scales, or
	 * the filter width.
	 *
	 * @param srcWidth the width of the source image.
	 * @param srcHeight the height of the source image.
	 * @param dstWidth the width of the destination image.
	 * @param dstHeight the height of the destination image.
	 */
	protected void invalidate(double srcWidth, double srcHeight, double dstWidth, double dstHeight) {
		invalidate((int) srcWidth, (int) srcHeight, (int) dstWidth, (int) dstHeight);
	}

	/**
	 * Invalidates cached data. Such as the pixel contributions, the scales, or
	 * the filter width.
	 *
	 * @param srcWidth the width of the source image.
	 * @param srcHeight the height of the source image.
	 * @param dstWidth the width of the destination image.
	 * @param dstHeight the height of the destination image.
	 */
	protected void invalidate(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
		this.srcWidth = srcWidth;
		this.srcHeight = srcHeight;
		this.dstWidth = dstWidth;
		this.dstHeight = dstHeight;
		invalidate();
	}

	/**
	 * Invalidates cached data. Such as the pixel contributions, the scales, or
	 * the filter width.
	 */
	protected void invalidate() {
		this.xscale = (double) dstWidth / srcWidth;
		this.yscale = (double) dstHeight / srcHeight;
		this.fwidth = filterf.getSupport();
		this.Y = calcContribY();
		this.X = calcContribX();
	}

	/**
	 * Calculates the Y pixel contributions.
	 *
	 * @return the Y pixel contributions.
	 */
	protected PixelContributions[] calcContribY() {
		final PixelContributions[] contribY = new PixelContributions[dstHeight];
		for (int i = 0; i < contribY.length; i++) {
			contribY[i] = new PixelContributions();
		}

		if (yscale < 1.0) {
			double width = fwidth / yscale;
			double fscale = 1.0 / yscale;

			// reduce to point sampling
			if (width <= 0.5) {
				width = 0.5 + 1.0e-6;
				fscale = 1.0;
			}

			for (int i = 0; i < dstHeight; i++) {
				contribY[i].contributions = new PixelContribution[(int) (width * 2.0 + 1)];
				contribY[i].n = 0;

				final double center = i / yscale + PIXEL_SHIFT;
				final int left = (int) Math.ceil(center - width);
				final int right = (int) Math.floor(center + width);

				double density = 0.0;

				for (int j = left; j <= right; j++) {
					double weight = filterf.filter((center - j) / fscale) / fscale;
					final int n = padPixel(j, srcHeight);
					final int k = contribY[i].n++;
					contribY[i].contributions[k] = new PixelContribution(n, weight);
					density += weight;
				}

				// normalize
				if ((density != 0.0) && (density != 1.0)) {
					density = 1.0 / density;
					for (int k = 0; k < contribY[i].n; k++) {
						contribY[i].contributions[k].weight *= density;
					}
				}
			}
		} else {
			for (int i = 0; i < dstHeight; i++) {
				contribY[i].contributions = new PixelContribution[(int) (fwidth * 2.0 + 1)];
				contribY[i].n = 0;

				final double center = i / yscale + PIXEL_SHIFT;
				final int left = (int) Math.ceil(center - fwidth);
				final int right = (int) Math.floor(center + fwidth);

				for (int j = left; j <= right; j++) {
					double weight = filterf.filter(center - j);
					final int n = padPixel(j, srcHeight);
					final int k = contribY[i].n++;
					contribY[i].contributions[k] = new PixelContribution(n, weight);
				}
			}
		}

		return contribY;
	}

	/**
	 * Calculates the X pixel contributions.
	 *
	 * @return the X pixel contributions.
	 */
	protected PixelContributions[] calcContribX() {
		final PixelContributions[] contribX = new PixelContributions[dstWidth];
		for (int i = 0; i < contribX.length; i++) {
			contribX[i] = new PixelContributions();
		}

		double width;
		double fscale;
		double center;
		double weight;

		if (xscale < 1.0) {
			for (int i = 0; i < dstWidth; i++) {
				width = fwidth / xscale;
				fscale = 1.0 / xscale;

				// reduce to point sampling
				if (width <= 0.5) {
					width = 0.5 + 1.0e-6;
					fscale = 1.0;
				}

				contribX[i].n = 0;
				contribX[i].contributions = new PixelContribution[(int) (width * 2.0 + 1)];

				center = i / xscale + PIXEL_SHIFT;
				final int left = (int) Math.ceil(center - width);	// assumes width <= 0.5
				final int right = (int) Math.floor(center + width);

				double density = 0.0;

				for (int j = left; j <= right; j++) {
					weight = filterf.filter((center - j) / fscale) / fscale;
					final int n = padPixel(j, srcWidth);
					final int k = contribX[i].n++;
					contribX[i].contributions[k] = new PixelContribution(n, weight);

					density += weight;
				}

				// normalize
				if ((density != 0.0) && (density != 1.0)) {
					density = 1.0 / density;
					for (int k = 0; k < contribX[i].n; k++) {
						contribX[i].contributions[k].weight *= density;
					}
				}
			}
		} else {
			for (int i = 0; i < dstWidth; i++) {
				contribX[i].n = 0;
				contribX[i].contributions = new PixelContribution[(int) (fwidth * 2.0 + 1)];

				center = i / xscale + PIXEL_SHIFT;
				final int left = (int) Math.ceil(center - fwidth);
				final int right = (int) Math.floor(center + fwidth);

				for (int j = left; j <= right; j++) {
					weight = filterf.filter(center - j);
					final int n = padPixel(j, srcWidth);
					final int k = contribX[i].n++;
					contribX[i].contributions[k] = new PixelContribution(n, weight);
				}
			}
		}

		return contribX;
	}

	/**
	 * Image padding with clamped indexing. Repeats border pixels/samples.
	 *
	 * @param n index of the pixel/sample.
	 * @param len length of the row (or column).
	 * @return clamped index.
	 */
	protected static int padPixel(int n, int len) {
		if (n < 0) {
			return 0;
		}
		if (n >= len) {
			return len - 1;
		}
		return n;
	}

	/**
	 * A pixel contribution.
	 */
	protected static class PixelContribution {

		/**
		 * Index of the pixel.
		 */
		public int pixel;

		/**
		 * Weight of the pixel.
		 */
		public double weight;

		/**
		 * Creates a new pixel contribution.
		 *
		 * @param pixel index of the pixel.
		 * @param weight weight of the pixel.
		 */
		public PixelContribution(int pixel, double weight) {
			this.pixel = pixel;
			this.weight = weight;
		}

	}

	/**
	 * Pixel contributions of a row, or column.
	 */
	protected static class PixelContributions {

		/**
		 * Number of pixel contributions.
		 */
		public int n;

		/**
		 * The pixel contributions.
		 */
		public PixelContribution[] contributions;

	}

	/**
	 * Returns the index into a linear pixel array.
	 *
	 * @param row row of the pixel.
	 * @param col column of the pixel.
	 * @param width width of the image.
	 * @return index of the pixel in the pixel array.
	 */
	protected int index(int row, int col, int width) {
		return col * width + row;
	}

	/**
	 * Unpacks a packed rgba integer.
	 *
	 * @param pixel the rgba pixel.
	 * @return the rgba samples.
	 */
	protected int[] unpack(int pixel) {
		return new int[]{
			pixel & 255,
			(pixel >> 8) & 255,
			(pixel >> 16) & 255,
			(pixel >> 24) & 255
		};
	}

	/**
	 * Pixel weight.
	 */
	protected static class PixelWeight {

		public final double[] rgba;

		/**
		 * Creates a new temporary pixel weight.
		 */
		public PixelWeight() {
			this.rgba = new double[]{0, 0, 0, 0};
		}

		/**
		 * Adds pixel weight to the given band.
		 *
		 * @param i index of the band.
		 * @param weight the additional pixel weight.
		 */
		public void add(int i, double weight) {
			rgba[i] += weight;
		}

		/**
		 * Sets/finalizes the pixel weight. Called after all pixel weights have
		 * been added.
		 *
		 * @param i index of the band.
		 * @param pel pixel weight.
		 * @param bPelDelta pixel delta.
		 * @param max max. value of the pixel weight.
		 */
		public void set(int i, double pel, boolean bPelDelta, double max) {
			final double weight = bPelDelta ? Math.round(rgba[i] * 255) / 255f : pel;
			if (weight < 0) {
				rgba[i] = 0;
			} else if (weight > max) {
				rgba[i] = max;
			} else {
				rgba[i] = weight;
			}
		}

		/**
		 * Returns the pixel weight as packed integer.
		 *
		 * @return the pixel weight as packed integer.
		 */
		public int getPackedInt() {
			return ((int) rgba[3] << 24) | ((int) rgba[2] << 16) | ((int) rgba[1] << 8) | (int) rgba[0];
		}
	}

}
