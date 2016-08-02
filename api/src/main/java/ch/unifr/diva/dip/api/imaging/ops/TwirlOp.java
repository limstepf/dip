package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.interpolation.Interpolant;
import ch.unifr.diva.dip.api.imaging.mapper.TwirlMapper;
import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;

/**
 * Non-linear twirl (or twist) filter.
 */
public class TwirlOp extends GeometricTransformOp<TwirlMapper> {

	/**
	 * Creates a twirl filter with a reflective image padder, and without clamping.
	 *
	 * @param cx relative X position of the center of the vortex (in 0..1).
	 * @param cy relative Y position of the center of the vortex (in 0..1).
	 * @param strength strength of the vortex/distortion.
	 * @param interpolant the interpolation method.
	 */
	public TwirlOp(double cx, double cy, double strength, Interpolant interpolant) {
		this(cx, cy, strength, interpolant, ImagePadder.Type.REFLECTIVE.getInstance());
	}

	/**
	 * Creates a twirl filter without clamping.
	 *
	 * @param cx relative X position of the center of the vortex (in 0..1).
	 * @param cy relative Y position of the center of the vortex (in 0..1).
	 * @param strength strength of the vortex/distortion.
	 * @param interpolant the interpolation method.
	 * @param padder the image padder.
	 */
	public TwirlOp(double cx, double cy, double strength, Interpolant interpolant, ImagePadder padder) {
		this(cx, cy, strength, interpolant, padder, null, null);
	}

	/**
	 * Creates a twirl filter with clamping.
	 *
	 * @param cx relative X position of the center of the vortex (in 0..1).
	 * @param cy relative Y position of the center of the vortex (in 0..1).
	 * @param strength strength of the vortex/distortion.
	 * @param interpolant the interpolation method.
	 * @param padder the image padder.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 */
	public TwirlOp(double cx, double cy, double strength, Interpolant interpolant, ImagePadder padder, double[] min, double[] max) {
		super(getTwirlMapper(cx, cy, strength), interpolant, padder, min, max);
	}

	/**
	 * Returns the twirl mapper.
	 *
	 * @return the twirl mapper.
	 */
	public TwirlMapper getMapper() {
		return this.mapper;
	}

	protected static TwirlMapper getTwirlMapper(double cx, double cy, double strength) {
		return new TwirlMapper(cx, cy, strength);
	}

}
