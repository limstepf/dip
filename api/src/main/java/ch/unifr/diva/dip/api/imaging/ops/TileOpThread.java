package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.scanners.ImageTiler;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Image processing worker thread for {@code TileParallelizable}
 * {@code BufferedImageOp}s.
 *
 * @param <T> subclass of BufferedImageOp and Parallelizable.
 */
public class TileOpThread<T extends BufferedImageOp & TileParallelizable> extends Thread {

	private final T op;
	private final ImageTiler tiler;
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
	public TileOpThread(T op, ImageTiler tiler, BufferedImage src, BufferedImage dst) {
		this.op = op;
		this.tiler = tiler;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public void run() {
		TileParallelizable.process(op, tiler, src, dst);
	}

}
