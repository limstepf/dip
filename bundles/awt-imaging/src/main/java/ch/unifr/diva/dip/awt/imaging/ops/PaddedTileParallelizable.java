package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.PaddedImageTiler;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The padded parallelizable interface marks a {@code BufferedImageOp} to be
 * compatible with being wrapped and run by {@code ConcurrentOp} using a
 * {@code PaddedImageTiler}. While the assumptions made by the ordinary
 * {@code Paralellizable} interface still hold, a padded parallelizable image
 * operation works with larger, that is padded tiles, where the full region
 * minus an additionally defined inner region is read-only, whereas that inner
 * region is read- and writable.
 *
 * <p>
 * Padded parallelization is usefull mostly for image operations working with
 * images wrapped by an {@code ImagePadder}, s.t. if an image for processing is
 * tiled <em>edge handling</em> still only occurs at the edges of the image, but
 * not in it (at the edges of tiles), as would happen with a
 * {@code SimpleImageTiler}.
 *
 * <p>
 * Note that a {@code RasterScanner} should iterate over the given
 * {@code writableRegion} instead of iterating over the source or destination
 * image (or raster).
 *
 * @see TileParallelizable
 */
public interface PaddedTileParallelizable extends TileParallelizable<PaddedImageTiler> {

	// TODO: eh, now that the InverseMappedTileParallelizable interface is here,
	// wouldn't that do the same, but a bit cheaper?!

	/*
	 * Make sure to overwrite the filter method for single threaded execution,
	 * and put the following bit in the implementing class. We can't implement it
	 * here as default method, since it will be implemented/overwritten in the
	 * base class NullOp, so that wouldn't have any effect:
	 *
	 * {@code
	 *		@Override
	 *		public BufferedImage filter(BufferedImage src, BufferedImage dst) {
	 *			return filter(src, dst, new Rectangle(src.getWidth(), src.getHeight()));
	 *		}
	 * }
	 */
//
	/**
	 * Performs a single-input/output operation on a BufferedImage within a
	 * writable region only.
	 *
	 * @param src the source image to be filtered.
	 * @param dst the destination image in which to store the results.
	 * @param writableRegion the writable region within the source image.
	 * Everything outside this region is read-only.
	 * @return the filtered image.
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dst, Rectangle writableRegion);

	@Override
	default void process(PaddedImageTiler tiler, BufferedImage src, BufferedImage dst) {
		ConcurrentTileOp.processPaddedTiles(
				this,
				tiler,
				src,
				dst
		);
	}

	/**
	 * Returns an appropriate image tiler with as much padding as needed.
	 *
	 * @param src the source image to be filtered.
	 * @param width the width of the tile (writable/unpadded region).
	 * @param height the height of the tile (writable/unpadded region).
	 * @return a padded image tiler.
	 */
	@Override
	public PaddedImageTiler getImageTiler(BufferedImage src, BufferedImage dst, int width, int height);

}
