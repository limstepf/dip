
package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Color band visualization filter.
 */
public class ColorBandVisualizationOp extends NullOp {

	private final SimpleColorModel cm;
	private int band;

	public ColorBandVisualizationOp(SimpleColorModel cm) {
		this(cm, 0);
	}

	public ColorBandVisualizationOp(SimpleColorModel cm, int band) {
		this.cm = cm;
		this.band = band;
	}

	public void setBand(int band) {
		this.band = band;
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

	public BufferedImage createCompatibleDestImage(BufferedImage src, SimpleColorModel cm) {
		return new BufferedImage(src.getWidth(), src.getHeight(), cm.getBandVisualizationImageType());
	}

}
