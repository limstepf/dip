package ch.unifr.diva.dip.awt.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;

/**
 * Image tiler base class.
 *
 * @param <T> type of the tile.
 */
public abstract class ImageTiler<T extends Rectangle> implements Iterator<T>, Iterable<T> {

	protected final int imageWidth;
	protected final int imageHeight;
	protected final int width;
	protected final int height;
	protected final int rows;
	protected final int cols;
	protected final int last;
	protected int index;

	/**
	 * Creates a new image tiler with quadratic/square tiles. Returned tiles may
	 * be larger than specified, but never smaller (except for the case where
	 * the tile size is larger than the image size).
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

		// in cases of remainding pixels we want to end up with larger tiles
		// than specified, not smaller ones (see note below)!
		if (this.width >= this.imageWidth) {
			this.cols = 1;
		} else {
			this.cols = (int) Math.floor(this.imageWidth / this.width);
		}

		if (this.height >= this.imageHeight) {
			this.rows = 1;
		} else {
			this.rows = (int) Math.floor(this.imageHeight / this.height);
		}

		this.last = this.cols * this.rows - 1;
	}

	@Override
	public synchronized boolean hasNext() {
		return this.index < last;
	}

	@Override
	public synchronized T next() {
		if (!hasNext()) {
			return null;
		}

		this.index++;
		final int x = getX();
		final int y = getY();
		return getTile(x, y, getWidth(x), getHeight(y));
	}

	protected abstract T getTile(int x, int y, int width, int height);

	protected int getX() {
		return this.index % this.cols * this.width;
	}

	protected int getY() {
		return this.index / this.cols * this.height;
	}

	/*
	 * For thread safety with binary images we need to make sure to not produce
	 * smaller rest-tiles at the border, but larger tiles an iteration earlier!
	 * See notes in ProcessorBase.java, getOptimalTileSize() method.
	 */
	protected int getWidth(int x) {
		final int w = x + this.width;
		final int remainder = this.imageWidth - w;
		return (remainder < this.width) ? this.width + remainder : this.width;
	}

	protected int getHeight(int y) {
		final int h = y + this.height;
		final int remainder = this.imageHeight - h;
		return (remainder < this.height) ? this.height + remainder : this.height;
	}

	@Override
	public synchronized Iterator<T> iterator() {
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
