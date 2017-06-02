package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import ch.unifr.diva.dip.awt.imaging.scanners.Location;
import ch.unifr.diva.dip.awt.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Linear scaling filter.
 */
public class RescaleOp extends NullOp implements TileParallelizable {

	private final boolean[] abs;
	private final double[] gain;
	private final double[] bias;
	private final double[] min;
	private final double[] max;
	private final SamplePrecision precision;

	private final double[][] srcRange; // band -> [min, max]
	private final double[][] dstRange;

	/**
	 * Gain-bias rescaling.
	 * <pre>I'(x,y) = gain * I(x,y) + bias</pre>
	 *
	 * @param abs take the absolute value of the sample first (per band). Can be
	 * null.
	 * @param gain gain per band.
	 * @param bias bias per band.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 * @param precision desired output sample precision.
	 */
	public RescaleOp(boolean[] abs, double[] gain, double[] bias, double[] min, double[] max, SamplePrecision precision) {
		this.abs = abs;
		this.gain = gain;
		this.bias = bias;
		this.min = min;
		this.max = max;
		this.precision = precision;

		this.srcRange = null;
		this.dstRange = null;
	}

	/**
	 * Sample domain to sample domain rescaling.
	 * <pre>I(x,y) in [a,b] -> I'(x,y) in [a',b']</pre>
	 *
	 * @param srcRange sample domain range of the source per band.
	 * @param dstRange sample domain range of the destination per band.
	 * @param precision desired output sample precision.
	 */
	public RescaleOp(double[][] srcRange, double[][] dstRange, SamplePrecision precision) {
		this.abs = null;
		this.gain = null;
		this.bias = null;
		this.min = null;
		this.max = null;
		this.precision = precision;

		this.srcRange = srcRange;
		this.dstRange = dstRange;
	}

	private boolean rescaleByRange() {
		return (this.srcRange != null && this.dstRange != null);
	}

	// check whether at least one band need to take the absolute sample value
	private boolean takeAbsValue() {
		if (this.abs == null) {
			return false;
		}
		for (int i = 0; i < abs.length; i++) {
			if (abs[i]) {
				return true;
			}
		}
		return false;
	}

	// return absolute sample value, or not, depending on spec.
	private double abs(int band, double value) {
		return abs[band] ? Math.abs(value) : value;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, precision);
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		if (rescaleByRange()) {
			filterByRange(srcRaster, dstRaster);
		} else {
			filterByGainBias(srcRaster, dstRaster);
		}

		return dst;
	}

	private void filterByGainBias(WritableRaster srcRaster, WritableRaster dstRaster) {
		if (takeAbsValue()) {
			for (Location pt : new RasterScanner(getRasterWithLeastBands(srcRaster, dstRaster))) {
				dstRaster.setSample(
						pt.col,
						pt.row,
						pt.band,
						ImagingUtils.clamp(
								this.gain[pt.band] * abs(pt.band, srcRaster.getSampleFloat(pt.col, pt.row, pt.band)) + this.bias[pt.band],
								this.min[pt.band],
								this.max[pt.band]
						)
				);
			}
		} else {
			for (Location pt : new RasterScanner(getRasterWithLeastBands(srcRaster, dstRaster))) {
				dstRaster.setSample(
						pt.col,
						pt.row,
						pt.band,
						ImagingUtils.clamp(
								this.gain[pt.band] * srcRaster.getSampleFloat(pt.col, pt.row, pt.band) + this.bias[pt.band],
								this.min[pt.band],
								this.max[pt.band]
						)
				);
			}
		}
	}

	private void filterByRange(WritableRaster srcRaster, WritableRaster dstRaster) {
		final WritableRaster raster = getRasterWithLeastBands(srcRaster, dstRaster);
		final double[] ratio = new double[raster.getNumBands()];
		for (int i = 0; i < ratio.length; i++) {
			ratio[i] = (this.dstRange[i][1] - this.dstRange[i][0]) / (this.srcRange[i][1] - this.srcRange[i][0]);
		}

		for (Location pt : new RasterScanner(raster)) {
			dstRaster.setSample(
					pt.col,
					pt.row,
					pt.band,
					ImagingUtils.rescale(
							srcRaster.getSampleFloat(pt.col, pt.row, pt.band),
							ratio[pt.band],
							this.srcRange[pt.band][1],
							this.dstRange[pt.band][1]
					)
			);
		}
	}

}
