package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.scanners.ImageTiler;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.concurrent.Callable;

/**
 * Image processing worker for {@code TileParallelizable}
 * {@code BufferedImageOp}s. Indended to be used by an ExecutorService.
 *
 * @param <T> subclass of BufferedImageOp and Parallelizable.
 */
public class TileOpCallable<T extends BufferedImageOp & TileParallelizable> implements Callable<Void> {

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
	public TileOpCallable(T op, ImageTiler tiler, BufferedImage src, BufferedImage dst) {
		this.op = op;
		this.tiler = tiler;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public Void call() throws Exception {
		TileParallelizable.process(op, tiler, src, dst);
		return null;
	}

}
