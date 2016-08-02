package ch.unifr.diva.dip.api.imaging.ops;

import ch.unifr.diva.dip.api.imaging.BufferedMatrix;
import ch.unifr.diva.dip.api.imaging.ImagingUtils;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * {@code NullOp} is a general base class implementation of
 * {@code BufferedImageOp} that can be easily subclassed to provide specific
 * image processing functions. While {@code NullOp} is a complete implementation
 * of {@code BufferedImageOp} (that simply copies each source sample to the
 * destination), it is meant to be subclassed by overriding the {@code filter}
 * method.
 */
public class NullOp implements BufferedImageOp {

	/**
	 * Available sample precisions. This enumeration is mostly used to give a
	 * list of options to produce an output image (e.g. SampleRescaler).
	 */
	public enum SamplePrecision {

		/**
		 * Bit sample precision. Single-band BufferedImage of type
		 * TYPE_BYTE_BINARY.
		 */
		BIT,
		/**
		 * Byte sample precision. Depending on the number of bands this will be
		 * a BufferedImage of type TYPE_BYTE_GRAY (single-band), TYPE_INT_RGB (3
		 * bands), or TYPE_INT_ARGB (4 bands).
		 */
		BYTE,
		/**
		 * Float sample precision. BufferedMatrix of floats with the same number
		 * of bands as the input image.
		 */
		FLOAT
	}

	/**
	 * Creates a zeroed destination image of the same size and type as the given
	 * source image.
	 *
	 * @param src the source image.
	 * @return the zeroed destination image.
	 */
	public BufferedImage createCompatibleDestImage(BufferedImage src) {
		return createCompatibleDestImage(src, src.getColorModel());
	}

	@Override
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
		// buffered matrix?
		if (src instanceof BufferedMatrix) {
			final BufferedMatrix mat = (BufferedMatrix) src;
			return new BufferedMatrix(
					mat.getWidth(),
					mat.getHeight(),
					mat.getNumBands(),
					mat.getSampleDataType(),
					mat.getInterleave()
			);
		}

		// common type?
		if (src.getType() != 0) {
			return new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
		}

		// custom type
		return createCompatibleDestImage(src.getRaster().getBounds(), dstCM);
	}

	/**
	 * Creates a zeroed destination image of the same type as the given source
	 * image, and with specified dimensions.
	 *
	 * @param src the source image.
	 * @param bounds bounds/size of the destination image.
	 * @param dstCM ColorModel of the destination.
	 * @return the zeroed destination image.
	 */
	public BufferedImage createCompatibleDestImage(BufferedImage src, Rectangle bounds, ColorModel dstCM) {
		// buffered matrix?
		if (src instanceof BufferedMatrix) {
			final BufferedMatrix mat = (BufferedMatrix) src;
			return new BufferedMatrix(
					bounds.width,
					bounds.height,
					mat.getNumBands(),
					mat.getSampleDataType(),
					mat.getInterleave()
			);
		}

		// common type?
		if (src.getType() != 0) {
			return new BufferedImage(bounds.width, bounds.height, src.getType());
		}

		// custom type
		return createCompatibleDestImage(bounds, dstCM);
	}

	/**
	 * Creates a zeroed destination image with the correct size and number of
	 * bands. This always produces a {@code BufferedImage}, never a
	 * {@code BufferedMatrix}.
	 *
	 * @param bounds bounds/size of the destination image.
	 * @param dstCM ColorModel of the destination.
	 * @return the zeroed destination image.
	 */
	public BufferedImage createCompatibleDestImage(Rectangle bounds, ColorModel dstCM) {
		return new BufferedImage(
				dstCM,
				dstCM.createCompatibleWritableRaster(bounds.width, bounds.height),
				dstCM.isAlphaPremultiplied(),
				null
		);
	}

	/**
	 * Creates a zeroed destination image with desired sample precision and the
	 * number of bands of the source image.
	 *
	 * @param src the source image, used to determine the number of bands in the
	 * returned image.
	 * @param precision sample precision of the destination image.
	 * @return the zeroed destination image.
	 */
	public BufferedImage createCompatibleDestImage(BufferedImage src, SamplePrecision precision) {
		return createCompatibleDestImage(
				src.getWidth(),
				src.getHeight(),
				precision,
				ImagingUtils.numBands(src)
		);
	}

	/**
	 * Creates a zeroed destination image with desired dimensions, sample
	 * precision, and number of bands.
	 *
	 * @param width width of the destination image.
	 * @param height height of the destination image.
	 * @param precision sample precision of the destination image.
	 * @param numBands number of bands of the destination image.
	 * @return the zeroed destination image.
	 */
	public BufferedImage createCompatibleDestImage(int width, int height, SamplePrecision precision, int numBands) {
		switch (precision) {
			case FLOAT:
				return new BufferedMatrix(width, height, numBands);

			case BIT:
				return new BufferedImage(
						width,
						height,
						BufferedImage.TYPE_BYTE_BINARY
				);

			case BYTE:
			default:
				return new BufferedImage(
						width,
						height,
						getCompatibleBufferdImageType(numBands)
				);
		}
	}

	/**
	 * Creates a zeroed destination matrix with the correct size, precision and
	 * number of bands.
	 *
	 * @param bounds bounds/size of the destination matrix.
	 * @param src source matrix the destination matrix should be compatible to
	 * (defines numBands, sampleDataType, and interleave).
	 * @return the zeroed destination matrix.
	 */
	public BufferedMatrix createCompatibleDestMatrix(Rectangle bounds, BufferedMatrix src) {
		return new BufferedMatrix(
				bounds.width,
				bounds.height,
				src.getNumBands(),
				src.getSampleDataType(),
				src.getInterleave()
		);
	}

	/**
	 * Returns a BufferedImage type compatible for the given SimpleColorModel.
	 * Handles images with byte precision and 1, 3, or 4 bands.
	 *
	 * @param cm the SimpleColorModel to get a compatible BufferedImage type
	 * for.
	 * @return the BufferdImage type.
	 */
	public int getCompatibleBufferdImageType(SimpleColorModel cm) {
		return getCompatibleBufferdImageType(cm.numBands());
	}

	/**
	 * Returns a suitable BufferedImage type for the given number of bands,
	 * assuming a sample precision of BYTE.
	 *
	 * @param numBands the number of bands (1, 3, or 4).
	 * @return a BufferedImage Type supporting the desired number of bands.
	 */
	public int getCompatibleBufferdImageType(int numBands) {
		switch (numBands) {
			case 1:
				return BufferedImage.TYPE_BYTE_GRAY;

			case 4:
				return BufferedImage.TYPE_INT_ARGB;

			case 3:
			default:
				return BufferedImage.TYPE_INT_RGB;
		}
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null) {
			dst = createCompatibleDestImage(src, src.getColorModel());
		}

		final WritableRaster srcRaster = src.getRaster();
		final WritableRaster dstRaster = dst.getRaster();

		for (Location pt : new RasterScanner(src, true)) {
			final int sample = srcRaster.getSample(pt.col, pt.row, pt.band);
			dstRaster.setSample(pt.col, pt.row, pt.band, sample);
		}

		return dst;
	}

	@Override
	public Rectangle2D getBounds2D(BufferedImage src) {
		return src.getRaster().getBounds();
	}

	@Override
	public Point2D getPoint2D(Point2D src, Point2D dst) {
		if (dst == null) {
			dst = (Point2D) src.clone();
		} else {
			dst.setLocation(src);
		}

		return dst;
	}

	@Override
	public RenderingHints getRenderingHints() {
		return null;
	}

	/**
	 * Returns the raster with the least number of bands.
	 *
	 * @param <T> subclass of {@code Raster}.
	 * @param a the first raster.
	 * @param b the second raster.
	 * @return the raster with the least number of bands, or the first raster if
	 * both bands have the same number of bands.
	 */
	protected <T extends Raster> T getRasterWithLeastBands(T a, T b) {
		return (b.getNumBands() < a.getNumBands()) ? b : a;
	}

}
