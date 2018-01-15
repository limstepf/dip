package ch.unifr.diva.dip.awt.imaging.scanners;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
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

	/**
	 * Creates a new, banded raster scanner for the given image. Iterates over
	 * all pixels and all bands in the image.
	 *
	 * @param image the image to scan.
	 */
	public RasterScanner(BufferedImage image) {
		this(image, true);
	}

	/**
	 * Creates a new raster scanner for the given image. Iterates over all
	 * pixels, and if desired all bands in the image.
	 *
	 * @param image the image to scan.
	 * @param isBanded iterates over all pixels and bands if {@code true}, over
	 * all pixels only if {@code false}.
	 */
	public RasterScanner(BufferedImage image, boolean isBanded) {
		this(
				image.getRaster(),
				isBanded ? ImagingUtils.numBands(image) : 1
		);
	}

	/**
	 * Creates a new, banded raster scanner for the given raster. Iterates over
	 * all pixels and all bands in the raster.
	 *
	 * @param raster the raster to scan.
	 */
	public RasterScanner(WritableRaster raster) {
		this(raster, raster.getNumBands());
	}

	/**
	 * Creates a new, banded raster scanner for the given raster. Iterates over
	 * all pixels and all bands in the raster.
	 *
	 * @param raster the raster to scan.
	 * @param isBanded iterates over all pixels and bands if {@code true}, over
	 * all pixels only if {@code false}.
	 */
	public RasterScanner(WritableRaster raster, boolean isBanded) {
		this(
				raster,
				isBanded ? raster.getNumBands() : 1
		);
	}

	/**
	 * Creates a new raster scanner for the given raster. Iterates over all
	 * pixels, and given number of bands in the raster.
	 *
	 * @param raster the raster to scan.
	 * @param numBands the number of bands in the raster.
	 */
	public RasterScanner(WritableRaster raster, int numBands) {
		this(
				new Rectangle(raster.getWidth(), raster.getHeight()),
				numBands
		);
	}

	/**
	 * Creates a new raster scanner for the given region. Iterates over all
	 * discrete positions in the region.
	 *
	 * @param region the region to scan.
	 */
	public RasterScanner(Rectangle region) {
		this(region, 1);
	}

	/**
	 * Creates a new raster scanner for the given region. Iterates over all
	 * discrete positions and given number of bands in the region.
	 *
	 * @param region the region to scan.
	 * @param numBands the number of bands in the region.
	 */
	public RasterScanner(Rectangle region, int numBands) {
		super(region, numBands);

		this.index = -1;
		this.maxIndex = region.width * region.height * numBands;
	}

	/**
	 * Returns the current row.
	 *
	 * @return the current row.
	 */
	protected int getRow() {
		return index / (numBands * region.width) + region.y;
	}

	/**
	 * Returns the current column.
	 *
	 * @return the current column.
	 */
	protected int getCol() {
		return (index / numBands) % region.width + region.x;
	}

	/**
	 * Returns the current band.
	 *
	 * @return the current band.
	 */
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
