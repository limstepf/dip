package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.ImageTiler;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.concurrent.Callable;

/**
 * Image processing worker for {@code TileParallelizable}
 * {@code BufferedImageOp}s. Indended to be used by an ExecutorService.
 *
 * @param <T> subclass of BufferedImageOp and Parallelizable.
 * @param <S> class of the ImageTiler.
 */
public class TileOpCallable<T extends BufferedImageOp & TileParallelizable<S>, S extends ImageTiler<? extends Rectangle>> implements Callable<Void> {

	private final T op;
	private final S tiler;
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
	public TileOpCallable(T op, S tiler, BufferedImage src, BufferedImage dst) {
		this.op = op;
		this.tiler = tiler;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public Void call() throws Exception {
		op.process(tiler, src, dst);
		return null;
	}

}
