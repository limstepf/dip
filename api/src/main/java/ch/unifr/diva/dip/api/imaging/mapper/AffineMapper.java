package ch.unifr.diva.dip.api.imaging.mapper;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Affine transformation mapper.
 */
public class AffineMapper extends InverseMapper {

	protected final AffineTransform forward;
	protected final AffineTransform inverse;

	/**
	 * Creates a new affine mapper.
	 *
	 * @param T the affine transform.
	 * @throws NoninvertibleTransformException if the matrix cannot be inverted.
	 */
	public AffineMapper(AffineTransform T) throws NoninvertibleTransformException {
		this.forward = T;
		this.inverse = T.createInverse();
	}

	@Override
	public Point2D inverseTransform(Point2D dstPt, Point2D srcPt) {
		return this.inverse.transform(dstPt, srcPt);
	}

	@Override
	public Rectangle getDestinationBounds(BufferedImage src) {
		return this.forward.createTransformedShape(src.getRaster().getBounds()).getBounds();
	}

}
