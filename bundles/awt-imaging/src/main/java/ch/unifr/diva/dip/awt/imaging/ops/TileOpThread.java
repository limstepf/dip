package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.ImageTiler;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Image processing worker thread for {@code TileParallelizable}
 * {@code BufferedImageOp}s.
 *
 * @param <T> subclass of BufferedImageOp and Parallelizable.
 * @param <S> class of the ImageTiler.
 */
public class TileOpThread<T extends BufferedImageOp & TileParallelizable<S>, S extends ImageTiler<? extends Rectangle>> extends Thread {

	private final static AtomicInteger threadNumber = new AtomicInteger(1);
	private final T op;
	private final S tiler;
	private final BufferedImage src;
	private final BufferedImage dst;

	/**
	 * Creates a new image processing worker thread.
	 *
	 * @param op the image filter.
	 * @param tiler the image tiler.
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public TileOpThread(T op, S tiler, BufferedImage src, BufferedImage dst) {
		this.op = op;
		this.tiler = tiler;
		this.src = src;
		this.dst = dst;

		this.setName(
				"dip-tile-op-thread-"
				+ threadNumber.getAndIncrement()
		);
		this.setPriority(Thread.NORM_PRIORITY);
		this.setDaemon(false);
	}

	@Override
	public void run() {
		op.process(tiler, src, dst);
	}

}
