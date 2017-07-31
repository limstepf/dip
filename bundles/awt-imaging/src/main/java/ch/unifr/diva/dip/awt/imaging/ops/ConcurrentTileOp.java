package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.scanners.ImageTiler;
import ch.unifr.diva.dip.awt.imaging.scanners.PaddedImageTiler;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Wrapper to run {@code TileParallelizable} image processing filters in
 * parallel.
 *
 * @param <T> subclass of BufferedImageOp and TileParallelizable.
 * @param <S> class of the ImageTiler.
 */
public class ConcurrentTileOp<T extends BufferedImageOp & TileParallelizable<S>, S extends ImageTiler<? extends Rectangle>> extends NullOp {

	private final T op;
	private final int tileWidth;
	private final int tileHeight;
	private final int threadCount;
	private final DipThreadPool threadPool;

	/**
	 * Creates a new concurrent image processing filter. Uses as many threads as
	 * there are available processors reported by the Java runtime.
	 *
	 * @param op the image processing filter to be parallelized.
	 * @param tileWidth tile width.
	 * @param tileHeight tile height.
	 */
	public ConcurrentTileOp(T op, int tileWidth, int tileHeight) {
		this(op, tileWidth, tileHeight, Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Creates a new concurrent image processing filter.
	 *
	 * @param op the image processing filter to be parallelized.
	 * @param tileWidth tile width.
	 * @param tileHeight tile height.
	 * @param threadCount number of threads to be used.
	 */
	public ConcurrentTileOp(T op, int tileWidth, int tileHeight, int threadCount) {
		this(op, tileWidth, tileHeight, null, threadCount);
	}

	/**
	 * Create a new concurrent image processing filter.
	 *
	 * @param op the image processing filter to be parallelized.
	 * @param tileWidth tile width.
	 * @param tileHeight tile height.
	 * @param threadPool a DIP thread pool.
	 */
	public ConcurrentTileOp(T op, int tileWidth, int tileHeight, DipThreadPool threadPool) {
		this(op, tileWidth, tileHeight, threadPool, threadPool.poolSize());
	}

	/**
	 * Create a new concurrent image processing filter.
	 *
	 * @param op the image processing filter to be parallelized.
	 * @param tileWidth tile width.
	 * @param tileHeight tile height.
	 * @param threadPool a DIP thread pool, or {@code null}. If no thread pool
	 * is given, thread will be created manually.
	 * @param threadCount number of threads to be used.
	 */
	public ConcurrentTileOp(T op, int tileWidth, int tileHeight, DipThreadPool threadPool, int threadCount) {
		this.op = op;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.threadPool = threadPool;
		this.threadCount = threadCount;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = this.op.createCompatibleDestImage(src, src.getColorModel());
		}

		if (this.threadPool == null) {
			runOnThreads(src, dst);
		} else {
			runOnThreadPool(src, dst);
		}

		return dst;
	}

	private void runOnThreads(BufferedImage src, BufferedImage dst) {
		final S tiler = this.op.getImageTiler(src, dst, this.tileWidth, this.tileHeight);
		final Thread[] threads = new Thread[this.threadCount];

		for (int i = 0; i < this.threadCount; i++) {
			threads[i] = new TileOpThread<>(this.op, tiler, src, dst);
			threads[i].start();
		}

		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException ex) {
				// interrupted
			}
		}
	}

	private void runOnThreadPool(BufferedImage src, BufferedImage dst) {
		final S tiler = this.op.getImageTiler(src, dst, this.tileWidth, this.tileHeight);
		final List<Callable<Void>> callables = new ArrayList<>();

		for (int i = 0; i < this.threadCount; i++) {
			callables.add(new TileOpCallable<>(this.op, tiler, src, dst));
		}

		try {
			this.threadPool.getExecutorService().invokeAll(callables);
		} catch (InterruptedException ex) {
			// interrupted
		}
	}

	/**
	 * Processes tiles for as long as there are tiles left to be processed.
	 *
	 * @param <T> subclass of {@code TileParallelizable}.
	 * @param <S> class of the ImageTiler.
	 * @param op the {@code BufferedImageOp} implementing
	 * {@code TileParallelizable}.
	 * @param tiler the image tiler to ask for the next tile(s).
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public static <T extends TileParallelizable<S>, S extends ImageTiler<? extends Rectangle>> void processTiles(T op, S tiler, BufferedImage src, BufferedImage dst) {
		Rectangle tile;
		while ((tile = tiler.next()) != null) {
			final BufferedImage srcTile = src.getSubimage(tile.x, tile.y, tile.width, tile.height);
			final BufferedImage dstTile = dst.getSubimage(tile.x, tile.y, tile.width, tile.height);
			op.filter(srcTile, dstTile);
		}
	}

	/**
	 * Processes padded tiles for as long as there are tiles left to be
	 * processed.
	 *
	 * @param <T> subclass of {@code PaddedTileParallelizable}.
	 * @param op the {@code BufferedImageOp} implementing
	 * {@code PaddedTileParallelizable}.
	 * @param tiler the image tiler to ask for the next tile(s).
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public static <T extends PaddedTileParallelizable> void processPaddedTiles(T op, PaddedImageTiler tiler, BufferedImage src, BufferedImage dst) {
		PaddedImageTiler.PaddedTile tile;
		while ((tile = tiler.next()) != null) {
			final BufferedImage srcTile = src.getSubimage(tile.x, tile.y, tile.width, tile.height);
			final BufferedImage dstTile = dst.getSubimage(tile.x, tile.y, tile.width, tile.height);
			op.filter(srcTile, dstTile, tile.writableRegion);
		}
	}

	/**
	 * Processes (inversely mapped) tiles for as long as there are tiles left to
	 * be processed.
	 *
	 * @param <T> subclass of {@code InverseMappedTileParallelizable}.
	 * @param <S> class of the ImageTiler.
	 * @param op the {@code BufferedImageOp} implementing
	 * {@code InverseMappedTileParallelizable}.
	 * @param tiler the image tiler to ask for the next tile(s).
	 * @param src the source image.
	 * @param dst the destination image.
	 */
	public static <T extends InverseMappedTileParallelizable, S extends ImageTiler<? extends Rectangle>> void processMappedTiles(T op, S tiler, BufferedImage src, BufferedImage dst) {
		Rectangle tile;
		while ((tile = tiler.next()) != null) {
			final BufferedImage dstTile = dst.getSubimage(tile.x, tile.y, tile.width, tile.height);
			op.filter(src, dstTile);
		}
	}

}
