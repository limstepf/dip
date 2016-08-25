package ch.unifr.diva.dip.api.imaging.rescaling;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;

/**
 * Java Abstract Window Toolkit (AWT) native rescaling.
 */
public class AwtRescaling {

	private AwtRescaling() {
		// nope
	}

	/**
	 * Computes the scaled width of an image.
	 *
	 * @param src the source image.
	 * @param xscale the X scale.
	 * @return the width of the rescaled image.
	 */
	public static int scaledWidth(BufferedImage src, double xscale) {
		return (int) Math.round(src.getWidth() * xscale);
	}

	/**
	 * Computes the scaled height of an image.
	 *
	 * @param src the source image.
	 * @param yscale the Y scale.
	 * @return the height of the rescaled image.
	 */
	public static int scaledHeight(BufferedImage src, double yscale) {
		return (int) Math.round(src.getHeight() * yscale);
	}

	/**
	 * Creates a compatilble, scaled destination image.
	 *
	 * @param src the source image to be compatible to.
	 * @param xscale the X scale factor for the destination image.
	 * @param yscale the Y scale factor for the destination image.
	 * @return the new destination image.
	 */
	public static BufferedImage createCompatibleDestImage(BufferedImage src, double xscale, double yscale) {
		final int width = scaledWidth(src, xscale);
		final int height = scaledHeight(src, yscale);

		if (src.getType() != 0) {
			return new BufferedImage(width, height, src.getType());
		}

		final ColorModel cm = src.getColorModel();
		return new BufferedImage(
				cm,
				cm.createCompatibleWritableRaster(width, height),
				cm.isAlphaPremultiplied(),
				null
		);
	}

	/**
	 * Creates a compatilble destination image.
	 *
	 * @param src the source image to be compatible to.
	 * @param width the width of the destination image.
	 * @param height the height of the destination image.
	 * @return the new destination image.
	 */
	public static BufferedImage createCompatibleDestImage(BufferedImage src, int width, int height) {
		if (src.getType() != 0) {
			return new BufferedImage(width, height, src.getType());
		}

		final ColorModel cm = src.getColorModel();
		return new BufferedImage(
				cm,
				cm.createCompatibleWritableRaster(width, height),
				cm.isAlphaPremultiplied(),
				null
		);
	}

	/**
	 * Resizes bitmaps with nearest neighbor interpolation. This is equivalent
	 * to a {@code BOX} filter.
	 *
	 * @param src the source image.
	 * @param dst the destination image, or null to create a new one.
	 * @param xscale the X scale factor.
	 * @param yscale the Y scale factor.
	 * @return the destination image.
	 */
	public static BufferedImage zoomNN(BufferedImage src, BufferedImage dst, double xscale, double yscale) {
		return zoom(src, dst, xscale, yscale, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}

	/**
	 * Resizes bitmaps with bilinear interpolation. This is equivalent to a
	 * {@code TRIANGLE} filter.
	 *
	 * @param src the source image.
	 * @param dst the destination image, or null to create a new one.
	 * @param xscale the X scale factor.
	 * @param yscale the Y scale factor.
	 * @return the destination image.
	 */
	public static BufferedImage zoomBilinear(BufferedImage src, BufferedImage dst, double xscale, double yscale) {
		return zoom(src, dst, xscale, yscale, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	/**
	 * Resizes bitmaps with bicubic interpolation. This is equivalent to a
	 * {@code CATMULL_ROM} filter.
	 *
	 * @param src the source image.
	 * @param dst the destination image, or null to create a new one.
	 * @param xscale the X scale factor.
	 * @param yscale the Y scale factor.
	 * @return the destination image.
	 */
	public static BufferedImage zoomBicubic(BufferedImage src, BufferedImage dst, double xscale, double yscale) {
		return zoom(src, dst, xscale, yscale, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	}

	/**
	 * Resizes bitmaps.
	 *
	 * @param src the source image.
	 * @param dst the destination image, or null to create a new one.
	 * @param xscale the X scale factor.
	 * @param yscale the Y scale factor.
	 * @param interpolationHint the interpolation rendering hint.
	 * @return the destination image.
	 */
	public static BufferedImage zoom(BufferedImage src, BufferedImage dst, double xscale, double yscale, Object interpolationHint) {

		if (dst != null) {
			final int width = scaledWidth(src, xscale);
			final int height = scaledHeight(src, yscale);
			if (dst.getWidth() != width || dst.getHeight() != height) {
				dst = createCompatibleDestImage(src, width, height);
			}
		} else {
			dst = createCompatibleDestImage(src, xscale, yscale);
		}

		return zoom(src, dst, interpolationHint);
	}

	/**
	 * Resizes bitmaps.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @param interpolationHint the interpolation rendering hint.
	 * @return the destination image.
	 */
	public static BufferedImage zoom(BufferedImage src, BufferedImage dst, Object interpolationHint) {
		final Graphics2D g = dst.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
		g.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), null);
		g.dispose();

		return dst;
	}

	/**
	 * Scales an image with Bresenham. Super fast nearest neighbor resampling
	 * based on Bresenham's (line) algorithm.
	 *
	 * @param src the source image.
	 * @param dst the destination image, or null to create a new one.
	 * @param xscale the X scale factor.
	 * @param yscale the Y scale factor.
	 * @return the destination image.
	 */
	public static BufferedImage bresenham(BufferedImage src, BufferedImage dst, double xscale, double yscale) {
		if (dst != null) {
			final int width = scaledWidth(src, xscale);
			final int height = scaledHeight(src, yscale);
			if (dst.getWidth() != width || dst.getHeight() != height) {
				dst = createCompatibleDestImage(src, width, height);
			}
		} else {
			dst = createCompatibleDestImage(src, xscale, yscale);
		}

		return bresenham(src, dst);
	}

	/**
	 * Scales an image with Bresenham. Super fast nearest neighbor resampling
	 * based on Bresenham's (line) algorithm.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @return the destination image.
	 */
	public static BufferedImage bresenham(BufferedImage src, BufferedImage dst) {
		final DataBufferInt srcBuffer = (DataBufferInt) src.getRaster().getDataBuffer();
		final DataBufferInt dstBuffer = (DataBufferInt) dst.getRaster().getDataBuffer();

		final int h = src.getHeight();
		final int w = src.getWidth();
		final int newH = dst.getHeight();
		final int newW = dst.getWidth();

		final int yd = (h / newH) * w - w;
		final int yr = h % newH;
		final int xd = w / newW;
		final int xr = w % newW;
		int outOffset = 0;
		int inOffset = 0;

		for (int y = newH, ye = 0; y > 0; y--) {
			for (int x = newW, xe = 0; x > 0; x--) {
				dstBuffer.setElem(outOffset++, srcBuffer.getElem(inOffset));
				inOffset += xd;
				xe += xr;
				if (xe >= newW) {
					xe -= newW;
					inOffset++;
				}
			}
			inOffset += yd;
			ye += yr;
			if (ye >= newH) {
				ye -= newH;
				inOffset += w;
			}
		}

		return dst;
	}

}
