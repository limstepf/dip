package ch.unifr.diva.dip.awt.imaging.ops;

import ch.unifr.diva.dip.awt.imaging.ImagingUtils;
import java.awt.image.BufferedImage;

/**
 * Merge filter. Takes multiple single-banded images, and outputs a single,
 * multi-band image.
 */
public class MergeOp extends MultiImageOp {

	private final boolean[] abs;
	private final double[] gain;
	private final double[] bias;
	private final double[] min;
	private final double[] max;

	/**
	 * Merges multiple source images into a single image.
	 *
	 * @param sources single-banded source images.
	 * @param outputPrecision the output precision.
	 * @param outputNumBands the number of bands in the output image.
	 * @param abs take the absolute value of the sample first (per band).
	 * @param gain gain per band.
	 * @param bias bias per band.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 */
	public MergeOp(BufferedImage[] sources, SamplePrecision outputPrecision, int outputNumBands, boolean[] abs, double[] gain, double[] bias, double[] min, double[] max) {
		super(sources, outputPrecision, outputNumBands);
		this.abs = abs;
		this.gain = gain;
		this.bias = bias;
		this.min = min;
		this.max = max;
	}

	@Override
	public void combine(float[] samples_in, float[] samples_out) {
		for (int band = 0; band < samples_out.length; band++) {
			samples_out[band] = (float) ImagingUtils.clamp(
					gain[band] * abs(band, samples_in[band]) + bias[band],
					min[band],
					max[band]
			);
		}
	}

	private double abs(int band, double value) {
		return abs[band] ? Math.abs(value) : value;
	}

}
