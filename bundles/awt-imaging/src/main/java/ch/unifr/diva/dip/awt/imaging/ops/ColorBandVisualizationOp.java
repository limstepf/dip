package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Color band visualization filter.
 */
public class ColorBandVisualizationOp extends NullOp implements SimpleTileParallelizable {

	private final SimpleColorModel cm;
	private int band;

	/**
	 * Creates a new color band visualization filter.
	 *
	 * @param cm the simple color model.
	 */
	public ColorBandVisualizationOp(SimpleColorModel cm) {
		this(cm, 0);
	}

	/**
	 * Creates a new color band visualization filter.
	 *
	 * @param cm the simple color model.
	 * @param band the band to visualise.
	 */
	public ColorBandVisualizationOp(SimpleColorModel cm, int band) {
		this.cm = cm;
		this.band = band;
	}

	/**
	 * Sets the band to visualize.
	 *
	 * @param band the band.
	 */
	public void setBand(int band) {
		this.band = band;
	}

	/**
	 * Returns the band to visualize.
	 *
	 * @return the band.
	 */
	public int getBand() {
		return this.band;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, cm);
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		for (Location pt : new RasterScanner(src, false)) {
			cm.doBandVisualization(srcRaster, dstRaster, pt, this.band);
		}

		return dst;
	}

	/**
	 * Creates a compatible destination image for the band visualization.
	 *
	 * @param src the source image.
	 * @param cm the simple color model.
	 * @return a compatible destination image for the band visualization.
	 */
	public BufferedImage createCompatibleDestImage(BufferedImage src, SimpleColorModel cm) {
		return new BufferedImage(src.getWidth(), src.getHeight(), cm.getBandVisualizationImageType());
	}

}
