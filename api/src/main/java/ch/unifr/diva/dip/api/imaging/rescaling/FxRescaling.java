package ch.unifr.diva.dip.api.imaging.rescaling;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

/**
 * JavaFX "native" rescaling. That is from JavaFX {@code Image} to
 * {@code BufferedImage}, where we can apply native AWT scaling, get the result
 * as {@code BufferedImage} and convert back to a JavaFX {@code WritableImage}.
 * Sounds stupid as fuck, but this is the fastest we get if we want
 * interpolation other than bilinear... (JavaFX still doesn't respect the
 * setSmooth hint, and everything in the Prism pipeline is interpolated
 * bilinearly, and that's it. UGH.)
 */
public class FxRescaling {

	private FxRescaling() {
		// nope
	}

	/**
	 * Computes the scaled width of an image.
	 *
	 * @param src the source image.
	 * @param xscale the X scale.
	 * @return the width of the rescaled image.
	 */
	public static int scaledWidth(Image src, double xscale) {
		return (int) Math.round(src.getWidth() * xscale);
	}

	/**
	 * Computes the scaled height of an image.
	 *
	 * @param src the source image.
	 * @param yscale the Y scale.
	 * @return the height of the rescaled image.
	 */
	public static int scaledHeight(Image src, double yscale) {
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
	public static WritableImage createCompatibleDestImage(Image src, double xscale, double yscale) {
		final int width = scaledWidth(src, xscale);
		final int height = scaledHeight(src, yscale);

		return createCompatibleDestImage(src, width, height);
	}

	/**
	 * Creates a compatilble destination image.
	 *
	 * @param src the source image to be compatible to.
	 * @param width the width of the destination image.
	 * @param height the height of the destination image.
	 * @return the new destination image.
	 */
	public static WritableImage createCompatibleDestImage(Image src, int width, int height) {
		return new WritableImage(width, height);
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
	public static WritableImage zoomNN(Image src, WritableImage dst, double xscale, double yscale) {
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
	public static WritableImage zoomBilinear(Image src, WritableImage dst, double xscale, double yscale) {
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
	public static WritableImage zoomBicubic(Image src, WritableImage dst, double xscale, double yscale) {
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
	public static WritableImage zoom(Image src, WritableImage dst, double xscale, double yscale, Object interpolationHint) {
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
	public static WritableImage zoom(Image src, WritableImage dst, Object interpolationHint) {
		final BufferedImage srcBI = SwingFXUtils.fromFXImage(src, null);
		final BufferedImage dstBI = AwtRescaling.createCompatibleDestImage(srcBI, (int) dst.getWidth(), (int) dst.getHeight());
		AwtRescaling.zoom(srcBI, dstBI, interpolationHint);
		return SwingFXUtils.toFXImage(dstBI, dst);
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
	public static WritableImage bresenham(Image src, WritableImage dst, double xscale, double yscale) {
		return bresenham(src, dst, xscale, yscale, null, null);
	}

	/**
	 * Scales an image with Bresenham. Super fast nearest neighbor resampling
	 * based on Bresenham's (line) algorithm.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @param xscale the X scale factor.
	 * @param yscale the Y scale factor.
	 * @param srcBuffer the source buffer (to reuse), or null.
	 * @param dstBuffer the destination buffer (to reuse), or null.
	 * @return the destination image.
	 */
	public static WritableImage bresenham(Image src, WritableImage dst, double xscale, double yscale, int[] srcBuffer, int[] dstBuffer) {
		if (dst != null) {
			final int width = scaledWidth(src, xscale);
			final int height = scaledHeight(src, yscale);
			if (dst.getWidth() != width || dst.getHeight() != height) {
				dst = createCompatibleDestImage(src, width, height);
			}
		} else {
			dst = createCompatibleDestImage(src, xscale, yscale);
		}

		return bresenham(src, dst, srcBuffer, dstBuffer);
	}

	/**
	 * Scales an image with Bresenham. Super fast nearest neighbor resampling
	 * based on Bresenham's (line) algorithm.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @return the destination image.
	 */
	public static WritableImage bresenham(Image src, WritableImage dst) {
		return bresenham(src, dst, null, null);
	}

	/**
	 * Scales an image with Bresenham. Super fast nearest neighbor resampling
	 * based on Bresenham's (line) algorithm.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @param srcBuffer the source buffer (to reuse), or null.
	 * @param dstBuffer the destination buffer (to reuse), or null.
	 * @return the destination image.
	 */
	public static WritableImage bresenham(Image src, WritableImage dst, int[] srcBuffer, int[] dstBuffer) {
		final int h = (int) src.getHeight();
		final int w = (int) src.getWidth();
		final int newH = (int) dst.getHeight();
		final int newW = (int) dst.getWidth();

		final int yd = (h / newH) * w - w;
		final int yr = h % newH;
		final int xd = w / newW;
		final int xr = w % newW;
		int outOffset = 0;
		int inOffset = 0;

		final WritablePixelFormat format = PixelFormat.getIntArgbPreInstance();
		if (srcBuffer == null) {
			srcBuffer = new int[w * h];
		}
		src.getPixelReader().getPixels(0, 0, w, h, format, srcBuffer, 0, w);
		if (dstBuffer == null) {
			dstBuffer = new int[newW * newH];
		}

		for (int y = newH, ye = 0; y > 0; y--) {
			for (int x = newW, xe = 0; x > 0; x--) {
				dstBuffer[outOffset++] = srcBuffer[inOffset];
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

		dst.getPixelWriter().setPixels(0, 0, newW, newH, format, dstBuffer, 0, newW);
		return dst;
	}

}
