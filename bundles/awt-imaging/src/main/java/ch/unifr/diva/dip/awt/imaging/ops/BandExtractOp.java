package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.api.datastructures.BufferedMatrix;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Extracts a single band from a multiband image.
 */
public class BandExtractOp extends NullOp implements SimpleTileParallelizable {

	private int band;

	/**
	 * Creates a new band extract filter.
	 */
	public BandExtractOp() {
		this(0);
	}

	/**
	 * Creates a new band extract filter.
	 *
	 * @param band the band to be extracted.
	 */
	public BandExtractOp(int band) {
		this.band = band;
	}

	/**
	 * Sets the band to be extracted.
	 *
	 * @param band the band.
	 */
	public void setBand(int band) {
		this.band = band;
	}

	/**
	 * Returns the band to be extracted.
	 *
	 * @return the band.
	 */
	public int getBand() {
		return this.band;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, src.getColorModel());
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		for (Location pt : new RasterScanner(src, false)) {
			final int sample = srcRaster.getSample(pt.col, pt.row, this.band);
			dstRaster.setSample(pt.col, pt.row, 0, sample);
		}

		return dst;
	}

	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
		if (src instanceof BufferedMatrix) {
			final BufferedMatrix mat = (BufferedMatrix) src;
			return new BufferedMatrix(
					mat.getWidth(),
					mat.getHeight(),
					1,
					mat.getSampleDataType(),
					mat.getInterleave()
			);
		}

		return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	}

}
