package ch.unifr.diva.dip.awt.imaging.scanners;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract image scanner.
 */
abstract public class ImageScanner implements Iterable<Location>, Iterator<Location> {

	protected final static UnsupportedOperationException UNSUPPORTED_OPERATION_EXCEPTION = new UnsupportedOperationException();
	protected final static NoSuchElementException NO_SUCH_ELEMENT_EXCEPTION = new NoSuchElementException();
	protected final Location current;
	protected final Rectangle region;
	protected final int numBands;

	/**
	 * Creates a new image scanner for the given image.
	 *
	 * @param image the image to scan.
	 * @param useBands iterate over all samples in all bands in True, just over
	 * all pixels/locations otherwise.
	 */
	public ImageScanner(BufferedImage image, boolean useBands) {
		this(
				image.getRaster().getBounds(),
				useBands ? ImagingUtils.numBands(image) : 1
		);
	}

	/**
	 * Creates a new, unbanded image scanner for the given region. Iterates over
	 * all pixels/locations in the region.
	 *
	 * @param region the region to scan.
	 */
	public ImageScanner(Rectangle region) {
		this(region, 1);
	}

	/**
	 * Creates a new image scanner for the given region. Iterates over all
	 * samples in the given number of bands in the region.
	 *
	 * @param region the region to scan.
	 * @param numBands the number of bands in the region.
	 */
	public ImageScanner(Rectangle region, int numBands) {
		this.region = region;
		this.numBands = numBands;
		this.current = new Location(region.x, region.y, 0);
	}

	@Override
	public void remove() {
		throw UNSUPPORTED_OPERATION_EXCEPTION;
	}

	@Override
	public Iterator<Location> iterator() {
		return this;
	}

}
