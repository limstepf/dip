package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.interpolation.Interpolant;
import ch.unifr.diva.dip.api.imaging.mapper.InverseMapper;
import ch.unifr.diva.dip.api.imaging.padders.ImagePadder;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Geometric transformation operation.
 *
 * <p>
 * Note that some interpolation methods (e.g. cubic interpolation) absolutely
 * need clamping, or nasty errors/overflows will be produced.
 *
 * @param <M> class of the inverse mapper.
 */
public class GeometricTransformOp<M extends InverseMapper> extends NullOp implements InverseMappedTileParallelizable {

	protected final static double PIXEL_SHIFT = -0.5;

	protected final M mapper;
	protected final Interpolant interpolant;
	protected final ImagePadder padder;

	protected final double[] min;
	protected final double[] max;

	/**
	 * Creates a new geometric transform op with default image padder, and
	 * without clamping.
	 *
	 * @param mapper the inverse mapper.
	 * @param interpolant the interpolation method.
	 */
	public GeometricTransformOp(M mapper, Interpolant interpolant) {
		this(mapper, interpolant, mapper.getDefaultPadder());
	}

	/**
	 * Creates a new geometric transform op without clamping.
	 *
	 * @param mapper the inverse mapper.
	 * @param interpolant the interpolation method.
	 * @param padder the image padder.
	 */
	public GeometricTransformOp(M mapper, Interpolant interpolant, ImagePadder padder) {
		this(mapper, interpolant, padder, null, null);
	}

	/**
	 * Creates a new geometric transform op with clamping.
	 *
	 * @param mapper the inverse mapper.
	 * @param interpolant the interpolation method.
	 * @param padder the image padder.
	 * @param min minimum value per band used for clamping.
	 * @param max maximum value per band used for clamping.
	 */
	public GeometricTransformOp(M mapper, Interpolant interpolant, ImagePadder padder, double[] min, double[] max) {
		this.mapper = mapper;
		this.interpolant = interpolant;
		this.padder = padder;
		this.min = min;
		this.max = max;
	}

	protected boolean doClamp() {
		return min != null && max != null;
	}

	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
		return createCompatibleDestImage(
				src,
				this.mapper.getDestinationBounds(src),
				dstCM
		);
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, src.getColorModel());
		}

		final WritableRaster raster = dst.getRaster();
		final Raster dstParent = raster.getParent();
		final Rectangle bounds = raster.getBounds();
		final int offsetX;
		final int offsetY;
		if (dstParent == null) {
			offsetX = 0;
			offsetY = 0;
		} else {
			offsetX = raster.getSampleModelTranslateX();
			offsetY = raster.getSampleModelTranslateY();
		}

		this.mapper.initializeMapping(src);

		final int numBands = raster.getNumBands();
		final Object support = this.interpolant.getSupport();
		Point2D dstPt = new Point2D.Double();
		Point2D srcPt = new Point2D.Double();

		// not sure if the compiler would be smart enough here (probably),
		// ...anyways let's only clamp if needed, and only ask once about it.
		if (doClamp()) {
			for (Location pt : new RasterScanner(dst, false)) {
				setLocation(pt, dstPt, srcPt, offsetX, offsetY);

				for (int band = 0; band < numBands; band++) {
					raster.setSample(
							pt.col, pt.row, band,
							ImagingUtils.clamp(
									this.interpolant.interpolate(
											src, padder, srcPt, band, support
									),
									this.min[band],
									this.max[band]
							)
					);
				}
			}
		} else {
			for (Location pt : new RasterScanner(dst, false)) {
				setLocation(pt, dstPt, srcPt, offsetX, offsetY);

				for (int band = 0; band < numBands; band++) {
					raster.setSample(
							pt.col, pt.row, band,
							this.interpolant.interpolate(src, padder, srcPt, band, support)
					);
				}
			}
		}

		return dst;
	}

	protected void setLocation(Location pt, Point2D dstPt, Point2D srcPt, int offsetX, int offsetY) {
		dstPt.setLocation(
				pt.col - offsetX,
				pt.row - offsetY
		);
		srcPt = this.mapper.inverseTransform(dstPt, srcPt);
		srcPt.setLocation(
				srcPt.getX() + PIXEL_SHIFT,
				srcPt.getY() + PIXEL_SHIFT
		);
	}

}
