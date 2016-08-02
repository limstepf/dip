package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.scanners.ImageTiler;
import ch.unifr.diva.dip.api.imaging.scanners.PaddedImageTiler;
import ch.unifr.diva.dip.api.imaging.scanners.SimpleImageTiler;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * The parallelizable interface marks a {@code BufferedImageOp} to be compatible
 * with being wrapped and run by {@code ConcurrentOp}. The assumption is that an
 * implementing {@code BufferedImageOp} can be safely parallelized over tiles of
 * an image. I.e. the {@code BufferedImageOp} should not rely on the order of
 * pixel scanning or on image geometry, or faulty results may be produced when
 * parallelized this way.
 *
 * <p>
 * In case the {@code BufferedImageOp} works on images wrapped by an
 * {@code ImagePadder}, or otherwise needs read-only access to a bit more
 * neighbourhood, chances are {@code PaddedParallelizable} would be the correct
 * interface to implement.
 *
 * <p>
 * And if image geometry matters, have a look at the
 * {@code InverseMappedTileParallelizable} interface.
 *
 * @see PaddedTileParallelizable
 */
public interface TileParallelizable extends Parallelizable {

	/**
	 * Processes a tile.
	 *
	 * @param <T> class of the {@code BufferedImageOp} implementing
	 * {@code TileParallelizable}.
	 * @param op the {@code BufferedImageOp}.
	 * @param tiler the image tiler.
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public static <T extends BufferedImageOp & TileParallelizable> void process(T op, ImageTiler tiler, BufferedImage src, BufferedImage dst) {
		if (op instanceof PaddedTileParallelizable) {
			ConcurrentTileOp.processPaddedTiles(
					(PaddedTileParallelizable) op,
					(PaddedImageTiler) tiler,
					src,
					dst
			);
		} else if (op instanceof InverseMappedTileParallelizable) {
			ConcurrentTileOp.processMappedTiles(
					(InverseMappedTileParallelizable) op,
					tiler,
					src,
					dst
			);
		} else {
			ConcurrentTileOp.processTiles(
					op,
					tiler,
					src,
					dst
			);
		}
	}

	/**
	 * Returns an appropriate image tiler.
	 *
	 * @param src the source image to be filtered.
	 * @param dst the destination image.
	 * @param width the width of the tile.
	 * @param height the height of the tile.
	 * @return a simple image tiler.
	 */
	default ImageTiler getImageTiler(BufferedImage src, BufferedImage dst, int width, int height) {
		return new SimpleImageTiler(src, width, height);
	}

}
