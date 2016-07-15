package ch.unifr.diva.dip.api.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * A padded image tiler. This image tiler has a padded, read-only region around
 * an inner, readable and writable region.
 */
public class PaddedImageTiler extends ImageTiler<PaddedImageTiler.PaddedTile> {

	protected final int xPadding;
	protected final int yPadding;

	/**
	 * Creates a new image tiler with quadratic/square tiles. Returned tiles at
	 * the border of an image may be smaller than specified (i.e. there is no
	 * need to intersect a given tile with the image plane).
	 *
	 * @param src the source image to be tiled.
	 * @param size width/height of a tile.
	 * @param padding padding (applied to all four sides).
	 */
	public PaddedImageTiler(BufferedImage src, int size, int padding) {
		this(src, size, size, padding, padding);
	}

	/**
	 * Creates a new image tiler. Returned tiles at the border of an image may
	 * be smaller than specified (i.e. there is no need to intersect a given
	 * tile with the image plane).
	 *
	 * @param src the source image to be tiled.
	 * @param width width of a tile.
	 * @param height height of a tile.
	 * @param xPadding left/right padding (applied to the left and the right
	 * side).
	 * @param yPadding top/bottom padding (applied to the top and the bottom
	 * side).
	 */
	public PaddedImageTiler(BufferedImage src, int width, int height, int xPadding, int yPadding) {
		super(src, width, height);

		this.xPadding = xPadding;
		this.yPadding = yPadding;
	}

	@Override
	protected PaddedTile getTile(int x, int y, int width, int height) {
		int padX = x - this.xPadding;
		if (padX < 0) {
			padX = 0;
		}

		int padY = y - this.yPadding;
		if (padY < 0) {
			padY = 0;
		}

		int padWidth = width + (x - padX) + this.xPadding;
		if ((padX + padWidth) > this.imageWidth) {
			padWidth = this.imageWidth - padX;
		}

		int padHeight = height + (y - padY) + this.yPadding;
		if ((padY + padHeight) > this.imageHeight) {
			padHeight = this.imageHeight - padY;
		}

		final Rectangle writable = new Rectangle(x, y, width, height);
		return new PaddedTile(padX, padY, padWidth, padHeight, writable);
	}

	/**
	 * A padded image tile. The class extends {@code Rectangle} which represents
	 * the full region. Within this region is another, smaller region, which is
	 * the effectively processable, or writable, region. Everything outside that
	 * inner region, i.e. the padded region, is <em>read-only!</em>, and
	 * absolutely <em>not thread-safe</em> to write to.
	 */
	public static class PaddedTile extends Rectangle {

		private static final long serialVersionUID = 1L;

		/**
		 * Writable region within the padded tile.
		 */
		public final Rectangle writableRegion;

		/**
		 * Creates a new padded tile. The full region minus the writable region
		 * is considered read-only.
		 *
		 * @param x the X coordinate of the full region.
		 * @param y the Y coordinate of the full region.
		 * @param width the width of the full region.
		 * @param height the height of the full region.
		 * @param writableRegion the inner, writable region.
		 */
		public PaddedTile(int x, int y, int width, int height, Rectangle writableRegion) {
			super(x, y, width, height);

			this.writableRegion = writableRegion;
		}
	}

}
