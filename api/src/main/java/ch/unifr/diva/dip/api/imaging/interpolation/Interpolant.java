package ch.unifr.diva.dip.api.imaging.interpolation;

import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * The interpolant interface.
 */
public interface Interpolant {

	/**
	 * Takes a real-valued {@code Point2D} and produces the sample value at the
	 * location in the source image. An image padder must be supplied to allow
	 * interpolation at all spatial coordinates.
	 *
	 * @param src the source image.
	 * @param padder the image padder.
	 * @param point the real-valued point on the image plane.
	 * @param band the band in the source image.
	 * @param support the support window.
	 * @return the interpolated sample value.
	 */
	public double interpolate(BufferedImage src, ImagePadder padder, Point2D point, int band, Object support);

	/**
	 * Returns a new support (window). The support is a data structure (re-)used
	 * to hold neighbor samples that contribute to the interpolated sample by a
	 * weighting function.
	 *
	 * @return a new support (window).
	 */
	public Object getSupport();

}
