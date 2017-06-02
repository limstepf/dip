package ch.unifr.diva.dip.awt.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Simple ({@literal i.e.} most basic) image tiler. Uses a simple rectangle with
 * desired size (or smaller, if at the border) as a tile.
 */
public class SimpleImageTiler extends ImageTiler<Rectangle> {

	/**
	 * Creates a new image tiler with quadratic/square tiles. Returned tiles at
	 * the border of an image may be smaller than specified (i.e. there is no
	 * need to intersect a given tile with the image plane).
	 *
	 * @param src the source image to be tiled.
	 * @param size width/height of a tile.
	 */
	public SimpleImageTiler(BufferedImage src, int size) {
		this(src, size, size);
	}

	/**
	 * Creates a new image tiler. Returned tiles at the border of an image may
	 * be smaller than specified (i.e. there is no need to intersect a given
	 * tile with the image plane).
	 *
	 * @param src the source image to be tiled.
	 * @param width width of a tile.
	 * @param height height of a tile.
	 */
	public SimpleImageTiler(BufferedImage src, int width, int height) {
		super(src, width, height);
	}

	@Override
	protected Rectangle getTile(int x, int y, int width, int height) {
		return new Rectangle(x, y, width, height);
	}

}
