package ch.unifr.diva.dip.awt.imaging.mapper;

import ch.unifr.diva.dip.awt.imaging.padders.ImagePadder;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Inverse image mapper. Maps coordinates from the destination image back onto
 * the source image.
 */
public abstract class InverseMapper {

	/**
	 * Performs the inverse transform on the destination coordinates and stores
	 * the result in the {@code srcPt}.
	 *
	 * @param dstX the X position on the destination image plane.
	 * @param dstY the Y position on the destination image plane.
	 * @param srcPt the transformed point on the source image plane.
	 * @return the transformed point on the source image plane.
	 */
	public Point2D inverseTransform(int dstX, int dstY, Point2D srcPt) {
		return inverseTransform(new Point2D.Double(dstX, dstY), srcPt);
	}

	/**
	 * Performs the inverse transform on the destination point and stores the
	 * result in the {@code srcPt}.
	 *
	 * @param dstPt the point to transform on the destinatino image plane.
	 * @param srcPt the transformed point on the source image plane.
	 * @return the transformed point on the source image plane.
	 */
	public abstract Point2D inverseTransform(Point2D dstPt, Point2D srcPt);

	/**
	 * Returns the default image padder for the inverse mapper.
	 *
	 * @return an appropriate image padder.
	 */
	public ImagePadder getDefaultPadder() {
		return ImagePadder.Type.EXTENDED_BORDER.getInstance();
	}

	/**
	 * Hook method called once before inverse mapping of the source image.
	 *
	 * @param src the source image mapped to.
	 */
	public void initializeMapping(BufferedImage src) {

	}

	/**
	 * Returns the destination bounds.
	 *
	 * @param src the source image mapped to.
	 * @return the bounds (or size) of the destination image.
	 */
	public Rectangle getDestinationBounds(BufferedImage src) {
		return src.getRaster().getBounds();
	}

}
