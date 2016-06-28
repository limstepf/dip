
package ch.unifr.diva.dip.api.imaging.scanners;

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

	public BsqScanner(BufferedImage image) {
		this(image.getRaster());
	}

	public BsqScanner(WritableRaster raster) {
		this(
				new Rectangle(raster.getWidth(), raster.getHeight()),
				raster.getNumBands()
		);
	}

	public BsqScanner(Rectangle region, int numBands) {
		super(region, numBands);

		this.index = -1;
		this.bandArea = region.width * region.height;
		this.maxIndex = this.bandArea * numBands;
	}

	protected int getRow() {
		return (index % bandArea) / region.width;
	}

	protected int getCol() {
		return (index % bandArea) % region.width;
	}

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

		current.row = getRow();
		current.col = getCol();
		current.band = getBand();

		return current;
	}

}
