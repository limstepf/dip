package ch.unifr.diva.dip.awt.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Band-Sequential (BSQ) interleave scanner.
 */
public class BsqScanner extends ImageScanner {

	protected int index;
	protected final int maxIndex;
	protected final int bandArea;

	/**
	 * Creates a new BSQ scanner for the given region. Iterates over all
	 * discrete positions and given number of bands in the region.
	 *
	 * @param image the image to scan.
	 */
	public BsqScanner(BufferedImage image) {
		this(image.getRaster());
	}

	/**
	 * Creates a new BSQ scanner for the given region. Iterates over all
	 * discrete positions and given number of bands in the region.
	 *
	 * @param raster the raster to scan.
	 */
	public BsqScanner(WritableRaster raster) {
		this(
				new Rectangle(raster.getWidth(), raster.getHeight()),
				raster.getNumBands()
		);
	}

	/**
	 * Creates a new BSQ scanner for the given region. Iterates over all
	 * discrete positions and given number of bands in the region.
	 *
	 * @param region the region to scan.
	 * @param numBands the number of bands in the region.
	 */
	public BsqScanner(Rectangle region, int numBands) {
		super(region, numBands);

		this.index = -1;
		this.bandArea = region.width * region.height;
		this.maxIndex = this.bandArea * numBands;
	}

	/**
	 * Returns the current row.
	 *
	 * @return the current row.
	 */
	protected int getRow() {
		return (index % bandArea) / region.width;
	}

	/**
	 * Returns the current column.
	 *
	 * @return the current column.
	 */
	protected int getCol() {
		return (index % bandArea) % region.width;
	}

	/**
	 * Returns the current band.
	 *
	 * @return the current band.
	 */
	protected int getBand() {
		return index / bandArea;
	}

	@Override
	public boolean hasNext() {
		return index < (maxIndex - 1);
	}

	@Override
	public Location next() {
		if (!hasNext()) {
			throw NO_SUCH_ELEMENT_EXCEPTION;
		}
		index++;

		current.index = index;
		current.row = getRow();
		current.col = getCol();
		current.band = getBand();

		return current;
	}

}
