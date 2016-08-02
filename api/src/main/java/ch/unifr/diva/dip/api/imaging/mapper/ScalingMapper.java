package ch.unifr.diva.dip.api.imaging.mapper;

import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Scaling mapper. Special case of the affine transformation mapper with a way
 * cheaper implementation.
 */
public class ScalingMapper extends InverseMapper {

	protected double scaleX;
	protected double scaleY;

	/**
	 * Creates a new scaling mapper.
	 *
	 * @param scale scale factor for both dimensions.
	 */
	public ScalingMapper(double scale) {
		this(scale, scale);
	}

	/**
	 * Creates a new scaling mapper.
	 *
	 * @param scaleX X scale factor.
	 * @param scaleY Y scale factor.
	 */
	public ScalingMapper(double scaleX, double scaleY) {
		setScale(scaleX, scaleY);
	}

	/**
	 * Sets/updates the scale factors.
	 *
	 * @param scaleX
	 * @param scaleY
	 */
	final public void setScale(double scaleX, double scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/**
	 * Returns the X scale factor.
	 *
	 * @return the X scale factor.
	 */
	public double getScaleX() {
		return this.scaleX;
	}

	/**
	 * Returns the Y scale factor.
	 *
	 * @return the Y scale factor.
	 */
	public double getScaleY() {
		return this.scaleY;
	}

	@Override
	public Point2D inverseTransform(Point2D dstPt, Point2D srcPt) {
		if (srcPt == null) {
			srcPt = new Point2D.Double();
		}
		srcPt.setLocation(
				(dstPt.getX() / this.scaleX),
				(dstPt.getY() / this.scaleY)
		);
		return srcPt;
	}

	@Override
	public Rectangle getDestinationBounds(BufferedImage src) {
		return new Rectangle(
				0,
				0,
				(int) Math.round(src.getWidth() * this.scaleX),
				(int) Math.round(src.getHeight() * this.scaleY)
		);
	}

	@Override
	public ImagePadder getDefaultPadder() {
		return ImagePadder.Type.EXTENDED_BORDER.getInstance();
	}

}
