package ch.unifr.diva.dip.awt.imaging.mapper;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Non-linear twirl (or twisted) mapping.
 */
public class TwirlMapper extends InverseMapper {

	protected double cx;
	protected double cy;
	protected double strength;

	/**
	 * Creates a new twirl mapper.
	 *
	 * @param cx relative X position of the center of the vortex (in 0..1).
	 * @param cy relative Y position of the center of the vortex (in 0..1).
	 * @param strength strength of the vortex/distortion.
	 */
	public TwirlMapper(double cx, double cy, double strength) {
		setCenter(cx, cy);
		setStrength(strength);
	}

	/**
	 * Sets/updates the center of the vortex.
	 *
	 * @param cx relative X position of the center of the vortex (in 0..1).
	 * @param cy relative Y position of the center of the vortex (in 0..1).
	 */
	final public void setCenter(double cx, double cy) {
		this.cx = cx;
		this.cy = cy;
	}

	/**
	 * Returns the relative X position of the center of the vortex.
	 *
	 * @return the relative X position of the center of the vortex.
	 */
	public double getCenterX() {
		return this.cx;
	}

	/**
	 * Returns the relative Y position of the center of the vortex.
	 *
	 * @return the relative Y position of the center of the vortex.
	 */
	public double getCenterY() {
		return this.cy;
	}

	/**
	 * Sets/updates the strength of the vortex/distortion.
	 *
	 * @param strength the strength of the vortex/distortion.
	 */
	final public void setStrength(double strength) {
		this.strength = strength;
	}

	/**
	 * Returns the strength of the vortex/distortion.
	 *
	 * @return the strength of the vortex/distortion.
	 */
	public double getStrength() {
		return this.strength;
	}

	protected double centerX;
	protected double centerY;
	protected double minDim;

	@Override
	public void initializeMapping(BufferedImage src) {
		this.centerX = (int) (cx * src.getWidth()) + .5;
		this.centerY = (int) (cy * src.getHeight()) + .5;
		this.minDim = Math.min(src.getWidth(), src.getHeight());
	}

	@Override
	public Point2D inverseTransform(Point2D dstPt, Point2D srcPt) {
		if (srcPt == null) {
			srcPt = new Point2D.Double();
		}

		final double dx = dstPt.getX() - centerX;
		final double dy = dstPt.getY() - centerY;
		final double r = Math.sqrt(dx * dx + dy * dy);
		final double theta = Math.atan2(dy, dx);

		srcPt.setLocation(
				r * Math.cos(theta + strength * (r - minDim) / minDim) + centerX,
				r * Math.sin(theta + strength * (r - minDim) / minDim) + centerY
		);

		return srcPt;
	}

}
