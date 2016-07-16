
package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Extracts a single band from a multiband image.
 */
public class BandExtractOp extends NullOp implements TileParallelizable {

	private int band;

	public BandExtractOp() {
		this(0);
	}

	public BandExtractOp(int band) {
		this.band = band;
	}

	public void setBand(int band) {
		this.band = band;
	}

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
