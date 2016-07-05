package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.scanners.ImageTiler;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/**
 * Image processing worker thread.
 */
public class OpThread extends Thread {

	private final BufferedImageOp op;
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
	public OpThread(BufferedImageOp op, ImageTiler tiler, BufferedImage src, BufferedImage dst) {
		this.op = op;
		this.tiler = tiler;
		this.src = src;
		this.dst = dst;
	}

	@Override
	public void run() {
		Rectangle tile;
		while ((tile = tiler.next()) != null) {
			final BufferedImage srcTile = this.src.getSubimage(tile.x, tile.y, tile.width, tile.height);
			final BufferedImage dstTile = this.dst.getSubimage(tile.x, tile.y, tile.width, tile.height);
			this.op.filter(srcTile, dstTile);
		}
	}

}
