package ch.unifr.diva.dip.api.imaging.scanners;

import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * A raster scanner iterates over each sample in an image. This is equivalent to
 * a Band-Interleaved-by-Pixel (BIP) scanner.
 */
public class RasterScanner extends ImageScanner {

	protected int index;
	protected final int maxIndex;

	public RasterScanner(BufferedImage image) {
		this(image, true);
	}

	public RasterScanner(BufferedImage image, boolean isBanded) {
		this(
				image.getRaster(),
				isBanded ? ImagingUtils.numBands(image) : 1
		);
	}

	public RasterScanner(WritableRaster raster) {
		this(raster, raster.getNumBands());
	}

	public RasterScanner(WritableRaster raster, int numBands) {
		this(
				new Rectangle(raster.getWidth(), raster.getHeight()),
				numBands
		);
	}

	public RasterScanner(Rectangle region, int numBands) {
		super(region, numBands);

		this.index = -1;
		this.maxIndex = region.width * region.height * numBands;
	}

	protected int getRow() {
		return index / (numBands * region.width);
	}

	protected int getCol() {
		return (index / numBands) % region.width;
	}

	protected int getBand() {
		return index % numBands;
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
