package ch.unifr.diva.dip.api.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;

/**
 * Thread-safe image tiler for parallel image processing.
 */
public class ImageTiler implements Iterator<Rectangle>, Iterable<Rectangle> {

	private final int imageWidth;
	private final int imageHeight;
	private final int width;
	private final int height;
	private final int rows;
	private final int cols;
	private int index;

	/**
	 * Creates a new image tiler with quadratic/square tiles. Returned tiles at
	 * the border of an image may be smaller than specified (i.e. there is no
	 * need to intersect a given tile with the image plane).
	 *
	 * @param src the source image to be tiled.
	 * @param size width/height of a tile.
	 */
	public ImageTiler(BufferedImage src, int size) {
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
	public ImageTiler(BufferedImage src, int width, int height) {
		this.imageWidth = src.getWidth();
		this.imageHeight = src.getHeight();
		this.width = width;
		this.height = height;
		this.index = -1;
		this.cols = (int) Math.ceil(this.imageWidth / this.width)
				+ (this.imageWidth % this.width == 0 ? 0 : 1);
		this.rows = (int) Math.ceil(this.imageHeight / this.height)
				+ (this.imageHeight % this.height == 0 ? 0 : 1);
	}

	@Override
	public synchronized boolean hasNext() {
		return this.index < (this.cols * this.rows - 1);
	}

	@Override
	public synchronized Rectangle next() {
		if (!hasNext()) {
			return null;
		}

		this.index++;
		final int x = getX();
		final int y = getY();
		return new Rectangle(x, y, getWidth(x), getHeight(y));
	}

	private int getX() {
		return this.index % this.cols * this.width;
	}

	private int getY() {
		return this.index / this.cols * this.height;
	}

	private int getWidth(int x) {
		final int w = x + this.width;
		return (w > this.imageWidth) ? (this.width - (w - this.imageWidth)) : this.width;
	}

	private int getHeight(int y) {
		final int h = y + this.height;
		return (h > this.imageHeight) ? (this.height - (h - this.imageHeight)) : this.height;
	}

	@Override
	public synchronized Iterator<Rectangle> iterator() {
		return this;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@" + Integer.toHexString(this.hashCode())
				+ "{"
				+ ", width=" + this.width
				+ ", height=" + this.height
				+ ", rows=" + this.rows
				+ ", cols=" + this.cols
				+ "}";
	}

}
