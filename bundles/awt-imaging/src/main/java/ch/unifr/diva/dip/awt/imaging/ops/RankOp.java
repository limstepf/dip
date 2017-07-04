package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.api.datastructures.Mask;
import ch.unifr.diva.dip.awt.imaging.padders.ImagePadder;
import ch.unifr.diva.dip.awt.imaging.scanners.ImageTiler;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.PaddedImageTiler;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * A rank filter.
 */
public class RankOp extends NullOp implements PaddedTileParallelizable {

	/**
	 * Ranking methods.
	 */
	public enum Rank {

		/**
		 * The minimum value in a set.
		 */
		MIN() {

					@Override
					public int getRank(int cardinality) {
						return 0;
					}

				},
		/**
		 * The median, or middle value in an ordered set.
		 */
		MEDIAN() {

					@Override
					public int getRank(int cardinality) {
						return cardinality / 2;
					}

				},
		/**
		 * The maximum value in a set.
		 */
		MAX() {

					@Override
					public int getRank(int cardinality) {
						return cardinality - 1;
					}

				};

		/**
		 * Computes the effective rank (or index into the ordered set).
		 *
		 * @param cardinality cardinality of the mask.
		 * @return the effective rank (or index into the ordered set).
		 */
		public abstract int getRank(int cardinality);

	}

	private final Rank rank;
	private final Mask mask;
	private final ImagePadder padder;

	/**
	 * Creates a new rank filter.
	 *
	 * @param rank the rank of the filter.
	 * @param mask the mask.
	 * @param padder the image padder.
	 */
	public RankOp(Rank rank, Mask mask, ImagePadder padder) {
		this.rank = rank;
		this.mask = mask;
		this.padder = padder;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		return filter(src, dst, new Rectangle(src.getWidth(), src.getHeight()));
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst, Rectangle writableRegion) {
		if (dst == null) {
			dst = this.createCompatibleDestImage(src);
		}

		final int numBands = src.getRaster().getNumBands();
		final int cardinality = this.mask.cardinality();
		final int r = this.rank.getRank(cardinality);
		final int[] samples = new int[cardinality];
		final WritableRaster raster = dst.getRaster();

		for (Location pt : new RasterScanner(writableRegion, numBands)) {
			raster.setSample(
					pt.col,
					pt.row,
					pt.band,
					getSampleOfRank(src, pt, r, samples)
			);
		}

		return dst;
	}

	private int getSampleOfRank(BufferedImage src, Location pt, int rank, int[] samples) {
		int i = 0;
		for (Location m : new RasterScanner(this.mask.bounds())) {
			if (this.mask.get(m.col, m.row)) {
				samples[i++] = this.padder.getSample(
						src,
						pt.col + m.col,
						pt.row + m.row,
						pt.band
				);
			}
		}

		Arrays.sort(samples);
		return samples[rank];
	}

	@Override
	public ImageTiler getImageTiler(BufferedImage src, BufferedImage dst, int width, int height) {
		return new PaddedImageTiler(
				src,
				width,
				height,
				this.mask.width() / 2,
				this.mask.height() / 2
		);
	}

}
