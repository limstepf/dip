package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.interpolation.Interpolant;
import ch.unifr.diva.dip.api.imaging.mapper.ScalingMapper;
import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;

/**
 * Resample filter.
 */
public class ResampleOp extends GeometricTransformOp<ScalingMapper> {

	/**
	 * Creates a new resample op with an {@code EXTENDED_BORDER} image padder,
	 * and without clamping.
	 *
	 * @param scaleX X scale factor.
	 * @param scaleY Y scale factor.
	 * @param interpolant the interpolation method.
	 */
	public ResampleOp(double scaleX, double scaleY, Interpolant interpolant) {
		this(scaleX, scaleY, interpolant, ImagePadder.Type.EXTENDED_BORDER.getInstance());
	}

	/**
	 * Creates a new resample op with an {@code EXTENDED_BORDER} image padder,
	 * and with clamping.
	 *
	 * @param scaleX X scale factor.
	 * @param scaleY Y scale factor.
	 * @param interpolant the interpolation method.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 */
	public ResampleOp(double scaleX, double scaleY, Interpolant interpolant, double[] min, double[] max) {
		this(scaleX, scaleY, interpolant, ImagePadder.Type.EXTENDED_BORDER.getInstance(), min, max);
	}

	/**
	 * Creates a new resample op without clamping.
	 *
	 * @param scaleX X scale factor.
	 * @param scaleY Y scale factor.
	 * @param interpolant the interpolation method.
	 * @param padder the image padder.
	 */
	public ResampleOp(double scaleX, double scaleY, Interpolant interpolant, ImagePadder padder) {
		this(scaleX, scaleY, interpolant, padder, null, null);
	}

	/**
	 * Creates a new resample op with clamping.
	 *
	 * @param scaleX X scale factor.
	 * @param scaleY Y scale factor.
	 * @param interpolant the interpolation method.
	 * @param padder the image padder.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 */
	public ResampleOp(double scaleX, double scaleY, Interpolant interpolant, ImagePadder padder, double[] min, double[] max) {
		super(getScaleMapper(scaleX, scaleY), interpolant, padder, min, max);
	}

	/**
	 * Returns the scaling mapper.
	 *
	 * @return the scaling mapper.
	 */
	public ScalingMapper getMapper() {
		return this.mapper;
	}

	/**
	 * Returns an appropriate inverse mapper.
	 *
	 * @param scaleX X scale factor.
	 * @param scaleY Y scale factor.
	 * @return an inverse mapper.
	 */
	protected static ScalingMapper getScaleMapper(double scaleX, double scaleY) {
		return new ScalingMapper(scaleX, scaleY);
	}

}
