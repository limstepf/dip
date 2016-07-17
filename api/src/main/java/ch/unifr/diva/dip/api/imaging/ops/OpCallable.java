package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.scanners.ImageTiler;
import ch.unifr.diva.dip.api.imaging.scanners.PaddedImageTiler;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.concurrent.Callable;

/**
 * Image processing worker. Indended to be used by an ExecutorService.
 *
 * @param <T> subclass of BufferedImageOp and Parallelizable.
 */
public class OpCallable<T extends BufferedImageOp & TileParallelizable> implements Callable<Void> {

	private final T op;
	private final ImageTiler tiler;
	private final BufferedImage src;
	private final BufferedImage dst;

	/**
	 * Creates a new image processing worker.
	 *
	 * @param op the image filter.
	 * @param tiler the image tiler.
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public OpCallable(T op, ImageTiler tiler, BufferedImage src, BufferedImage dst) {
		this.op = op;
		this.tiler = tiler;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public Void call() throws Exception {
		if (this.op instanceof PaddedTileParallelizable) {
			ConcurrentTileOp.processPaddedTiles(
					(PaddedTileParallelizable) this.op,
					(PaddedImageTiler) this.tiler,
					this.src,
					this.dst
			);
		} else {
			ConcurrentTileOp.processTiles(
					this.op,
					this.tiler,
					this.src,
					this.dst
			);
		}

		return null;
	}

}
