package ch.unifr.diva.dip.imaging.rescaling;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.nio.IntBuffer;
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
	 * @param dst the destination image, or {@code null} to create a new one.
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
	 * @param dst the destination image, or {@code null} to create a new one.
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
	 * @param dst the destination image, or {@code null} to create a new one.
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
	 * @param dst the destination image, or {@code null} to create a new one.
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
		final BufferedImage dstBI = createCompatibleDestImage(srcBI, (int) dst.getWidth(), (int) dst.getHeight());
		zoom(srcBI, dstBI, interpolationHint);
		return SwingFXUtils.toFXImage(dstBI, dst);
	}

	/**
	 * Creates a compatilble destination image.
	 *
	 * @param src the source image to be compatible to.
	 * @param width the width of the destination image.
	 * @param height the height of the destination image.
	 * @return the new destination image.
	 */
	private static BufferedImage createCompatibleDestImage(BufferedImage src, int width, int height) {
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
	 * Resizes bitmaps.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @param interpolationHint the interpolation rendering hint.
	 * @return the destination image.
	 */
	private static BufferedImage zoom(BufferedImage src, BufferedImage dst, Object interpolationHint) {
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
	 * @param dst the destination image, or {@code null} to create a new one.
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
	 * @param srcBuffer the source buffer (to reuse), or {@code null}.
	 * @param dstBuffer the destination buffer (to reuse), or {@code null}.
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
	 * <p>
	 * See R. Ulichney, "Bresenham -style Sealing," Proceedings of the IS&amp;T
	 * Annual Conference (Cambridge, Mass., 1993): 101-103. For a short paper on
	 * this. The key idea is to fill accumulators with a fixed number of
	 * repeating pixels up to where they overflow to know when to sample from
	 * the next source pixel, all without multiplies during the main loop.
	 *
	 * @param src the source image.
	 * @param dst the destination image.
	 * @param srcBuffer the source buffer (to reuse), or {@code null}.
	 * @param dstBuffer the destination buffer (to reuse), or {@code null}.
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

		final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbPreInstance();
		if (srcBuffer == null) {
			srcBuffer = new int[w * h];
		}
		src.getPixelReader().getPixels(0, 0, w, h, format, srcBuffer, 0, w);
		if (dstBuffer == null) {
			dstBuffer = new int[newW * newH];
		}

		int outOffset = 0;
		int inOffset = 0;

		for (int y = 0, ye = 0; y < newH; y++) {
			for (int x = 0, xe = 0; x < newW; x++) {
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

	/**
	 * Bresenham upscaling with subpixel precision. Upscaling an image in a
	 * scroll-/zoompane is a bit more tricky since we might have to display only
	 * part of a scaled pixel (a scaled pixel consists/gets drawn with n
	 * repeated pixels, where n is the zoom factor).
	 *
	 * <pre>
	 * Source image:
	 *
	 *                1 pixel
	 *                |----|
	 *                .    .
	 *      a'---+----+----+----b'      a,b,c,d in double precision
	 *      |  a |. . |. . | b  |
	 *      +----+----+----+----+       a' = floor(a_x), floor(a_y)
	 *      |  . |    |    | .  |       b' = ceil(a_x),  floor(a_y)
	 *      +----+----+----+----+       c' = floor(a_x), ceil(a_y)
	 *      |  . |    |    | .  |       d' = ceil(a_x),  ceil(a_y)
	 *      +----+----+----+----+
	 *      |  c | . .| . .| d  |       - compute differences, and scale up
	 *      c'---+----+----+----d'        to get shiftX/Y, restX/Y
	 *
	 *
	 * Upscaled destination image:
	 *
	 *                  1 scaled pixel (n repeated pixels)
	 *                        |--------|
	 *                        .        .
	 *      +--------+--------+--------+--------+ . . -          -
	 *      |        |        |        |        |     | shiftY   |
	 *      |   A....|......................B   | . . -          |
	 *      |   .    |        |        |    .   |     |          |
	 *      +--------+--------+--------+--------+     |          |
	 *      |   .    |        |        |    .   |     |          |
	 *      |   .    |        |        |    .   |     |          |
	 *      |   .    |        |        |    .   |     |          |
	 *      +--------+--------+--------+--------+     | newH     | fullH
	 *      |   .    |        |        |    .   |     |          |
	 *      |   .    |        |        |    .   |     |          |
	 *      |   .    |        |        |    .   |     |          |
	 *      +--------+--------+--------+--------+     |          |
	 *      |   .    |        |        |    .   |     |          |
	 *      |   C....|......................D   | . . -          |
	 *      |        |        |        |        |     | restY    |
	 *      +--------+--------+--------+--------+ . . -          -
	 *      .   .                           .   .
	 *      .   .                           .   .
	 *      |---|-------- newW -------------|---|
	 *      shiftX                          restX
	 *
	 *      |---------- virtualW -----------|
	 *      |------------ fullW ----------------|
	 *
	 * </pre>
	 *
	 * @param src the source image.
	 * @param dst the destination image (defining {@code newW} and
	 * {@code newH}).
	 * @param shiftX number of shifted repeated pixels on the x-axis.
	 * @param restX the rest of the repeated pixels on the x-axis.
	 * @param shiftY number of the shifted repeated pixels on the y-axis.
	 * @param restY the rest of the repeated pixels on the y-axis.
	 * @param srcBuffer the source buffer (to reuse), or {@code null}.
	 * @param dstBuffer the destination buffer (to reuse), or {@code null}.
	 * @return the destination image.
	 */
	public static WritableImage bresenhamUpscaling(Image src, WritableImage dst, int shiftX, int restX, int shiftY, int restY, int[] srcBuffer, int[] dstBuffer) {
		final int h = (int) src.getHeight();
		final int w = (int) src.getWidth();
		final int newH = (int) dst.getHeight();
		final int newW = (int) dst.getWidth();
		final int yd = (restX > 0) ? -w + 1 : -w; // force 1x more xe accum. overflow with a rest
		final int virtualW = shiftX + newW;
		final int fullW = virtualW + restX;
		final int fullH = shiftY + newH + restY;

		final WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbPreInstance();
		if (srcBuffer == null) {
			srcBuffer = new int[w * h];
		}
		src.getPixelReader().getPixels(0, 0, w, h, format, srcBuffer, 0, w);
		if (dstBuffer == null) {
			dstBuffer = new int[newW * newH];
		}

		int outOffset = 0;
		int inOffset = 0;

		for (int y = 0, ye = (y == 0) ? shiftY * h : 0; y < newH; y++) {
			for (int x = 0, xe = (x == 0) ? shiftX * w : 0; x < newW; x++) {
				dstBuffer[outOffset++] = srcBuffer[inOffset];
				xe += w;
				if (xe >= virtualW) {
					xe -= fullW;
					inOffset++;
				}
			}
			inOffset += yd;
			ye += h;
			if (ye >= fullH) {
				ye -= fullH;
				inOffset += w;
			}
		}

		dst.getPixelWriter().setPixels(0, 0, newW, newH, format, dstBuffer, 0, newW);
		return dst;
	}

}
