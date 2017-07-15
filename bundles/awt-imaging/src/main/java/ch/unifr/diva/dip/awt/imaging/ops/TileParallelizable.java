package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.ImageTiler;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The parallelizable interface marks a {@code BufferedImageOp} to be compatible
 * with being wrapped and run by {@code ConcurrentOp}. The assumption is that an
 * implementing {@code BufferedImageOp} can be safely parallelized over tiles of
 * an image. I.e. the {@code BufferedImageOp} should not rely on the order of
 * pixel scanning or on image geometry, or faulty results may be produced when
 * parallelized this way.
 *
 * <p>
 * This interface probably shouldn't be used directly, since its methods aren't
 * implemented already (by means of default methods). Instead use one of the
 * following, fully implemented interfaces:
 *
 * <dl>
 * <dt>SimpleTileParallelizable</dt>
 * <dd>Offers write and read access to exactly the pixels in a tile.</dd>
 *
 * <dt>PaddedTileParallelizable</dt>
 * <dd>Additionally offers read-only access to some amount of neighour-pixels of
 * each tile.</dd>
 *
 * <dt>InverseMappedTileParallelizable</dt>
 * <dd>Additionally offers read-only access to the whole image (i.e image
 * geometry may matter again).</dd>
 *
 * </dl>
 *
 * @param <S> class of the ImageTiler.
 * @see SimpleTileParallelizable
 * @see PaddedTileParallelizable
 * @see InverseMappedTileParallelizable
 */
public interface TileParallelizable<S extends ImageTiler<? extends Rectangle>> extends Parallelizable {

	/**
	 * Processes the image.
	 *
	 * @param tiler the image tiler.
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public void process(S tiler, BufferedImage src, BufferedImage dst);

	/**
	 * Returns an appropriate image tiler.
	 *
	 * @param src the source image to be filtered.
	 * @param dst the destination image.
	 * @param width the width of the tile.
	 * @param height the height of the tile.
	 * @return a simple image tiler.
	 */
	public S getImageTiler(BufferedImage src, BufferedImage dst, int width, int height);

}
