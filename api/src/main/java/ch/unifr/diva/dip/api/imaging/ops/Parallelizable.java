package ch.unifr.diva.dip.api.imaging.ops;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * The parallelizable interface marks a {@code BufferedImageOp} to be compatible
 * with being wrapped and run by {@code ConcurrentOp}. This interface is not
 * supposed to be directly implemented by a {@code BufferedImageOp}, since it
 * does not specify the actual parallelization method.
 *
 * @see TileParallelizable, PaddedTileParallelizable
 */
public interface Parallelizable {

	/**
	 * Parallelizable mode.
	 */
	public enum Mode {

		/**
		 * Single threaded execution.
		 */
		SINGLE_THREADED,
		/**
		 * Tile parallelization. This includes
		 * <ul>
		 * <li>{@code TileParallelizable},</li>
		 * <li>{@code PaddedTileParallelizable}, and</li>
		 * <li>{@code InverseMappedTileParallelizable}.</li>
		 * </ul>
		 */
		TILE;
	}

	/**
	 * Returns the parallelizable mode to run a {@code BufferedImageOp} with
	 * specified thread pool size.
	 *
	 * @param op the {@code BufferedImageOp} we whish to parallelize.
	 * @param poolSize the thread pool size.
	 * @return a parallelizable mode.
	 */
	public static Mode getMode(BufferedImageOp op, int poolSize) {
		if (poolSize < 2) {
			return Mode.SINGLE_THREADED;
		}
		if (op instanceof TileParallelizable) {
			return Mode.TILE;
		}
		return Mode.SINGLE_THREADED;
	}

	/**
	 * Performs a single-input/output operation on a BufferedImage within a
	 * writable region only. This method is already defined by
	 * {@code BufferedImageOp}, but we declare it again with the same signature,
	 * s.t. we can just cast to a subclass of {@code Parallelizable} and still
	 * call the filter method of the {@code BufferedImageOp} that implements
	 * this interface.
	 *
	 * @param src the source image to be filtered.
	 * @param dst the destination image in which to store the results.
	 * @return the filtered image.
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dst);

}
