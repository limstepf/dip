package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.datastructures.DoubleMatrix;
import ch.unifr.diva.dip.api.datastructures.Kernel;
import ch.unifr.diva.dip.api.datastructures.Matrix;
import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;
import ch.unifr.diva.dip.api.imaging.scanners.ImageTiler;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.PaddedImageTiler;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Convolution filter.
 *
 * @param <T> class of the matrix backing the used kernel.
 */
public class ConvolutionOp<T extends Matrix> extends NullOp implements PaddedTileParallelizable {

	private final ImagePadder padder;
	private final Kernel<T> kernel; // *the* kernel, or just the row vector if separable
	private final Kernel<T> columnVector; // ...or, null if not separable
	private final ConvolutionOp[] convolutionPasses; // 1st and 2nd pass, null if not separable
	private final boolean isDoublePrecision;
	private final boolean[] abs;
	private final double[] gain;
	private final double[] bias;
	private final double[] min;
	private final double[] max;
	private final SamplePrecision precision;

	/**
	 * Creates the single-pass convolution filter for a double-pass convolution
	 * with a separable kernel.
	 *
	 * @param kernel the row or column vector.
	 * @param op the parent double-pass convolution filter.
	 */
	private ConvolutionOp(Kernel<T> kernel, ConvolutionOp op) {
		this.padder = op.padder;
		this.kernel = kernel;
		this.isDoublePrecision = this.kernel.matrix() instanceof DoubleMatrix;
		this.columnVector = null;
		this.convolutionPasses = null;

		this.abs = op.abs;
		this.gain = op.gain;
		this.bias = op.bias;
		this.min = op.min;
		this.max = op.max;
		this.precision = op.precision;
	}

	/**
	 * Creates a new convolution filter.
	 *
	 * <p>
	 * <em>Warning:</em> while this {@code BufferedImageOp} implements the
	 * {@code PaddedTileParallelizable} interface, errors will be produced if
	 * using a separable kernel (i.e. a row and a column vector) that is run
	 * concurrently with a tile approach. <br />
	 *
	 * To work around this limitation just do the two passes (in parallel)
	 * manually one after the other.
	 *
	 * @param rowVector the kernel (single-pass convolution), or the row vector
	 * for double-pass convolution with a separable kernel.
	 * @param columnVector null (single-pass convolution), or the column vector
	 * for double-pass convolution with a separable kernel.
	 * @param padder an image padder for edge handling.
	 * @param abs take the absolute value of the sample first (per band). Can be
	 * null.
	 * @param gain gain per band.
	 * @param bias bias per band.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 * @param precision desired output sample precision.
	 */
	public ConvolutionOp(Kernel<T> rowVector, Kernel<T> columnVector, ImagePadder padder,
			boolean[] abs, double[] gain, double[] bias, double[] min, double[] max, SamplePrecision precision) {
		this.padder = padder;
		this.kernel = rowVector;
		this.isDoublePrecision = this.kernel.matrix() instanceof DoubleMatrix;

		this.columnVector = columnVector;
		if (this.columnVector == null) {
			this.convolutionPasses = null;
		} else {
			this.convolutionPasses = new ConvolutionOp[2];
			this.convolutionPasses[0] = new ConvolutionOp(this.columnVector, this);
			this.convolutionPasses[1] = new ConvolutionOp(this.kernel, this);
		}

		this.abs = abs;
		this.gain = gain;
		this.bias = bias;
		this.min = min;
		this.max = max;
		this.precision = precision;
	}

	private float convolveAtFloat(BufferedImage src, Location pt) {
		float sum = 0;

		for (Location kernelPoint : new RasterScanner(kernel.bounds())) {
			sum += this.kernel.getValueFloat(kernelPoint.col, kernelPoint.row)
					* this.padder.getSampleFloat(
							src,
							pt.col - kernelPoint.col,
							pt.row - kernelPoint.row,
							pt.band
					);
		}

		return sum;
	}

	private double convolveAtDouble(BufferedImage src, Location pt) {
		double sum = 0;

		for (Location kernelPoint : new RasterScanner(kernel.bounds())) {
			sum += this.kernel.getValueDouble(kernelPoint.col, kernelPoint.row)
					* this.padder.getSampleDouble(
							src,
							pt.col - kernelPoint.col,
							pt.row - kernelPoint.row,
							pt.band
					);
		}

		return sum;
	}

	// check whether we do rescaling at all
	private boolean doRescale() {
		if (this.min == null || this.max == null || this.gain == null || this.bias == null) {
			return false;
		}
		return true;
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
	public ImageTiler getImageTiler(BufferedImage src, int width, int height) {
		return new PaddedImageTiler(
				src,
				width,
				height,
				this.kernel.width() / 2,
				this.kernel.height() / 2
		);
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst, Rectangle writableRegion) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, this.precision);
		}

		// separable double-pass convolution?
		// BUG/FEAUTRE: this produces errors if run in parallel (with padded-tiles)
		if (this.columnVector != null) {
			return this.convolutionPasses[1].filter(
					this.convolutionPasses[0].filter(
							src,
							null,
							writableRegion
					),
					dst,
					writableRegion
			);
		}

		// non-separable/single-pass convolution
		final WritableRaster dstRaster = dst.getRaster();
		final int numBands = Math.min(src.getRaster().getNumBands(), dstRaster.getNumBands());

		// things get a bit messy here to not have a thousand conditionals
		// withing the main loop, so:
		// 1) we're either working with a double, or a float matrix
		// 2) we do rescaling (gain * I(x,y) + bias), and clamping; or not
		// 3) we do take the absolute value (of some bands), or not at all
		if (this.isDoublePrecision) {
			// DOUBLE PRECISION
			if (!doRescale()) {
				if (takeAbsValue()) {
					// double | abs
					for (Location pt : new RasterScanner(writableRegion, numBands)) {
						dstRaster.setSample(pt.col, pt.row, pt.band,
								abs(pt.band, convolveAtDouble(src, pt))
						);
					}
				} else {
					// double
					for (Location pt : new RasterScanner(writableRegion, numBands)) {
						dstRaster.setSample(pt.col, pt.row, pt.band,
								convolveAtDouble(src, pt)
						);
					}
				}
			} else if (takeAbsValue()) {
				// double | abs | rescaling
				for (Location pt : new RasterScanner(writableRegion, numBands)) {
					dstRaster.setSample(pt.col, pt.row, pt.band,
							ImagingUtils.clamp(
									gain[pt.band] * abs(pt.band, convolveAtDouble(src, pt)) + bias[pt.band],
									min[pt.band],
									max[pt.band]
							)
					);
				}
			} else {
				// double | rescaling
				for (Location pt : new RasterScanner(writableRegion, numBands)) {
					dstRaster.setSample(pt.col, pt.row, pt.band,
							ImagingUtils.clamp(
									gain[pt.band] * convolveAtDouble(src, pt) + bias[pt.band],
									min[pt.band],
									max[pt.band]
							)
					);
				}
			}

		} else {
			// FLOAT PRECISION
			if (!doRescale()) {
				// float | abs
				if (takeAbsValue()) {
					for (Location pt : new RasterScanner(writableRegion, numBands)) {
						dstRaster.setSample(pt.col, pt.row, pt.band,
								abs(pt.band, convolveAtFloat(src, pt))
						);
					}
				} else {
					// float
					for (Location pt : new RasterScanner(writableRegion, numBands)) {
						dstRaster.setSample(pt.col, pt.row, pt.band,
								convolveAtFloat(src, pt)
						);
					}
				}
			} else if (takeAbsValue()) {
				// float | abs | rescaling
				for (Location pt : new RasterScanner(writableRegion, numBands)) {
					dstRaster.setSample(pt.col, pt.row, pt.band,
							ImagingUtils.clamp(
									gain[pt.band] * abs(pt.band, convolveAtFloat(src, pt)) + bias[pt.band],
									min[pt.band],
									max[pt.band]
							)
					);
				}
			} else {
				// float | rescaling
				for (Location pt : new RasterScanner(writableRegion, numBands)) {
					dstRaster.setSample(pt.col, pt.row, pt.band,
							ImagingUtils.clamp(
									gain[pt.band] * convolveAtFloat(src, pt) + bias[pt.band],
									min[pt.band],
									max[pt.band]
							)
					);
				}
			}

		}

		return dst;
	}

}
