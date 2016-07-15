package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.scanners.SimpleImageTiler;
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
 * In case the {@code BufferedImageOp} works on images wrapped by an
 * {@code ImagePadder}, or otherwise needs read-only access to a bit more
 * neighbourhood, chances are {@code PaddedParallelizable} would be the correct
 * interface to implement.
 *
 * @see PaddedParallelizable
 */
public interface Parallelizable {

	/**
	 * Returns an appropriate image tiler.
	 *
	 * @param src the source image to be filtered.
	 * @param width the width of the tile.
	 * @param height the height of the tile.
	 * @return a simple image tiler.
	 */
	default SimpleImageTiler getImageTiler(BufferedImage src, int width, int height) {
		return new SimpleImageTiler(src, width, height);
	}

}
