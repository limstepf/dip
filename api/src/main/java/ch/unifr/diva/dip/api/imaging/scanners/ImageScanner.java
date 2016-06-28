
package ch.unifr.diva.dip.api.imaging.scanners;

import ch.unifr.diva.dip.api.imaging.ImagingUtils;
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

	public ImageScanner(BufferedImage image, boolean useBands) {
		this(
				image.getRaster().getBounds(),
				useBands ? ImagingUtils.numBands(image) : 1
		);
	}

	public ImageScanner(Rectangle region, int numBands) {
		this.region = region;
		this.numBands = numBands;
		this.current = new Location();
	}

	public ImageScanner(Rectangle region) {
		this(region, 1);
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
