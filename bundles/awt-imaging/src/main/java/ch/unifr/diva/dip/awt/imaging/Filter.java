package ch.unifr.diva.dip.awt.imaging;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.api.utils.MathUtils;
import ch.unifr.diva.dip.awt.imaging.ops.ConcurrentTileOp;
import ch.unifr.diva.dip.awt.imaging.ops.Parallelizable;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Image filtering/processing.
 */
public class Filter {

	private Filter() {
		// nope :)
	}

	/**
	 * Filters an image in parallel (if possible). Runs filters in parallel if
	 * they implement the {@code Parallelizable} interface by wrapping them in a
	 * {@code ConcurrentOp} first, otherwise runs them as is (and single
	 * threaded).
	 *
	 * <p>
	 * <em>Warning:</em> Double-check if you shouldn't pass a compatible
	 * destination image from the original image filter (which might be wrapped
	 * if run in parallel).
	 *
	 * @param context the processor context.
	 * @param op the image filter (hopefully implementing the
	 * {@code Parallelizable} interface).
	 * @param src the source image.
	 * @return the filtered image.
	 */
	public static BufferedImage filter(ProcessorContext context, BufferedImageOp op, BufferedImage src) {
		return filter(context, op, src, null);
	}

	/**
	 * Filters an image in parallel (if possible). Runs filters in parallel if
	 * they implement the {@code Parallelizable} interface by wrapping them in a
	 * {@code ConcurrentOp} first, otherwise runs them as is (and single
	 * threaded).
	 *
	 * <p>
	 * <em>Warning:</em> make absolutely sure to give an actually compatible
	 * destination image in case the image filter doesn't rely on
	 * {@code createCompatibleDestImage(BufferedImage bi, ColorModel cm)}
	 * (overriding it is fine, since the method on the given image filter will
	 * be called if wrapped by {@code ConcurrentOp}). So if the filter depends
	 * on another method (with a different signature) to create a compatible
	 * destination image, better don't pass null here.
	 *
	 * @param context the processor context.
	 * @param op the image filter (hopefully implementing the
	 * {@code Parallelizable} interface).
	 * @param src the source image.
	 * @param dest the destination image, or null.
	 * @return the filtered image.
	 */
	public static BufferedImage filter(ProcessorContext context, BufferedImageOp op, BufferedImage src, BufferedImage dest) {
		return filter(context.getThreadPool(), op, src, dest);
	}

	/**
	 * Filters an image in parallel (if possible). Runs filters in parallel if
	 * they implement the {@code Parallelizable} interface by wrapping them in a
	 * {@code ConcurrentOp} first, otherwise runs them as is (and single
	 * threaded).
	 *
	 * <p>
	 * <em>Warning:</em> make absolutely sure to give an actually compatible
	 * destination image in case the image filter doesn't rely on
	 * {@code createCompatibleDestImage(BufferedImage bi, ColorModel cm)}
	 * (overriding it is fine, since the method on the given image filter will
	 * be called if wrapped by {@code ConcurrentOp}). So if the filter depends
	 * on another method (with a different signature) to create a compatible
	 * destination image, better don't pass null here.
	 *
	 * @param threadPool application wide thread pool/executor service
	 * @param op the image filter (hopefully implementing the
	 * {@code Parallelizable} interface).
	 * @param src the source image.
	 * @param dest the destination image, or null.
	 * @return the filtered image.
	 */
	public static BufferedImage filter(DipThreadPool threadPool, BufferedImageOp op, BufferedImage src, BufferedImage dest) {
		// get parallelizable mode of the op...
		final Parallelizable.Mode mode = Parallelizable.getMode(op, threadPool.poolSize());
		// ...and maybe have some further checks, if we not rather fall back to
		// single-threaded execution.
		switch (mode) {
			case TILE: {
				// do not parallelize super small images/tiles at all
				final int samples = src.getWidth() * src.getHeight() * src.getRaster().getNumBands();
				// TODO: find optimal threshold, this here is just a wild guess
				if (samples < (64 * 64 * 3)) {
					break;
				}

				final Rectangle tileSize = getOptimalTileSize(threadPool.poolSize(), src);
				if (tileSize.width > 0 && tileSize.height > 0) {
					final ConcurrentTileOp cop = new ConcurrentTileOp(
							op,
							tileSize.width,
							tileSize.height,
							threadPool
					);
					return cop.filter(src, dest);
				}
				break;
			}

			case SINGLE_THREADED:
			default:
				break;
		}

		// single threaded execution
		return op.filter(src, dest);
	}

	/**
	 * Returns the optimal tile size to run a filter in parallel.
	 *
	 * @param context the processor context.
	 * @param src the source image to be filtered.
	 * @return optimal tile size.
	 */
	public static Rectangle getOptimalTileSize(ProcessorContext context, BufferedImage src) {
		return getOptimalTileSize(context.getThreadPool().poolSize(), src);
	}

	/**
	 * Returns the optimal tile size to run a filter in parallel.
	 *
	 * @param numThreads number of threads.
	 * @param src the source image to be filtered.
	 * @return optimal tile size.
	 */
	public static Rectangle getOptimalTileSize(int numThreads, BufferedImage src) {
		/*
		 * Recall how ConcurrentOp uses it's workers/threads: we may just throw
		 * as many at it, and each worker will just get a new tile, as long as
		 * there are still tiles left to be processed. I.e. the tile size is not
		 * _that_ crucial after all. Optimally we'd like to have a tile size s.t.
		 * we end up with a number of tiles that can be evenly distributed to all
		 * threads (easier/less error with smaller tile size), while at the same
		 * time we'd like to have as few tiles as possible to reduce overhead -
		 * although that overhead isn't that great and fades in comparison to the
		 * usual workload, so...
		 *
		 * About THREAD SAFETY on binary images
		 * ------------------------------------
		 * Here comes a fun story: if we're operating on a BufferedImage of type
		 * BufferedImage.TYPE_BYTE_BINARY there will be errors/artifacts in the
		 * result IFF we're using a tile size that is not a power of two (and
		 * larger than or equal to 8)! Why? Most likely because the backing data
		 * buffer isn't a bit, but a byte buffer, so different threads want to
		 * manipulate different bits packed in the same bytes, hence the artifacts.
		 *
		 * As a solution we'd like to fix the tile size to be a power of two if
		 * we're dealing with a binary destination image. But since the destination
		 * image at this point might still be null, we can't reliably know that.
		 * Thanksfully the tile size doesn't matter much anyways, so we fix it in
		 * any case, while also making sure to not produce smaller tiles at the
		 * border, but larger ones an iteration earlier.
		 */
		// 1) a power of two
		final int w = MathUtils.nextPowerOfTwo(src.getWidth() / (numThreads + 1));
		final int h = MathUtils.nextPowerOfTwo(src.getHeight() / (numThreads + 1));

		// 2) at least 8
		return new Rectangle(
				Math.max(8, w),
				Math.max(8, h)
		);
	}

}
