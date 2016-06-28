package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Linear scaling filter.
 */
public class RescaleOp extends NullOp {

	/**
	 * Output sample precision of the produced image.
	 */
	public enum Precision {

		/**
		 * Bit sample precision. Single-band BufferedImage of type
		 * TYPE_BYTE_BINARY.
		 */
		BIT,
		/**
		 * Byte sample precision. Depending on the number of bands this will be
		 * a BufferedImage of type TYPE_BYTE_GRAY (single-band), TYPE_INT_RGB (3
		 * bands), or TYPE_INT_ARGB (4 bands).
		 */
		BYTE,
		/**
		 * Float sample precision. BufferedMatrix of floats with the same number
		 * of bands as the input image.
		 */
		FLOAT
	}

	private final double[] gain;
	private final double[] bias;
	private final double[] min;
	private final double[] max;
	private final Precision precision;

	private final double[][] srcRange; // band -> [min, max]
	private final double[][] dstRange;

	/**
	 * <pre>I'(x,y) = gain * I(x,y) + bias</pre>
	 *
	 * @param gain
	 * @param bias
	 * @param min
	 * @param max
	 * @param precision
	 */
	public RescaleOp(double[] gain, double[] bias, double[] min, double[] max, Precision precision) {
		this.gain = gain;
		this.bias = bias;
		this.min = min;
		this.max = max;
		this.precision = precision;

		this.srcRange = null;
		this.dstRange = null;
	}

	/**
	 * <pre>I(x,y) in [a,b] -> I'(x,y) in [a',b']</pre>
	 * 
	 * @param srcRange
	 * @param dstRange
	 * @param precision
	 */
	public RescaleOp(double[][] srcRange, double[][] dstRange, Precision precision) {
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

	// returns value specified for the band, or the last one defined
	private double get(int index, double[] values) {
		if (index < values.length) {
			return values[index];
		}

		return values[values.length - 1];
	}

	private double[] get(int index, double[][] values) {
		if (index < values.length) {
			return values[index];
		}
		return values[values.length - 1];
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, precision);
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();
		final int numBands = Math.min(
				ImagingUtils.numBands(src),
				ImagingUtils.numBands(dst)
		);

		if (rescaleByRange()) {
			filterByRange(srcRaster, dstRaster, numBands);
		} else {
			filterByGainBias(srcRaster, dstRaster, numBands);
		}

		return dst;
	}

	private void filterByGainBias(WritableRaster srcRaster, WritableRaster dstRaster, int numBands) {
		for (int band = 0; band < numBands; band++) {
			final double gainV = get(band, this.gain);
			final double biasV = get(band, this.bias);
			final double minV = get(band, this.min);
			final double maxV = get(band, this.max);

			for (Location pt : new RasterScanner(srcRaster, 1)) {
				dstRaster.setSample(
						pt.col,
						pt.row,
						band,
						ImagingUtils.clamp(
								gainV * srcRaster.getSampleFloat(pt.col, pt.row, band) + biasV,
								minV,
								maxV
						)
				);
			}
		}
	}

	private void filterByRange(WritableRaster srcRaster, WritableRaster dstRaster, int numBands) {
		for (int band = 0; band < numBands; band++) {
			final double[] srcRangeV = get(band, this.srcRange);
			final double[] dstRangeV = get(band, this.dstRange);
			final double ratio = (float)((dstRangeV[1] - dstRangeV[0]) / (srcRangeV[1] - srcRangeV[0]));

			for (Location pt : new RasterScanner(srcRaster, 1)) {
				dstRaster.setSample(
						pt.col,
						pt.row,
						band,
						ImagingUtils.rescale(
								srcRaster.getSampleFloat(pt.col, pt.row, band),
								ratio,
								srcRangeV[1],
								dstRangeV[1]
						)
				);
			}
		}
	}

	public BufferedImage createCompatibleDestImage(BufferedImage src, Precision precision) {
		final int n = ImagingUtils.numBands(src);
		switch (precision) {
			case FLOAT:
				return new BufferedMatrix(src.getWidth(), src.getHeight(), n);

			case BIT:
				return new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

			case BYTE:
			default:
				return new BufferedImage(src.getWidth(), src.getHeight(), getCompatibleType(n));
		}
	}

	private int getCompatibleType(int numBands) {
		switch (numBands) {
			case 4:
				return BufferedImage.TYPE_INT_ARGB;

			case 1:
				return BufferedImage.TYPE_BYTE_GRAY;

			case 3:
			default:
				return BufferedImage.TYPE_INT_RGB;
		}
	}

	/*
	 public BufferedImage createCompatibleDestImage(BufferedImage src, SimpleColorModel cm) {
	 if (cm.dataType().type().equals(BufferedMatrix.class)) {
	 return new BufferedMatrix(src.getWidth(), src.getHeight(), cm.numBands());
	 }
	 return new BufferedImage(src.getWidth(), src.getHeight(), getCompatibleType(cm));
	 }
	 */
}
