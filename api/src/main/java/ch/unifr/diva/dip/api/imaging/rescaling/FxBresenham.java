package ch.unifr.diva.dip.api.imaging.rescaling;

import ch.unifr.diva.dip.api.utils.AdaptiveIntBuffer;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 * Nearest neighbor resampling. Uses the super fast Bresenham algorithm, and the
 * JavaFX API to operate directly on JavaFX WritableImages. The kicker is, that
 * we do not get direct access to the buffers, so we wrap it all up nicely in
 * this class.
 *
 * <p>
 * This is the fastest NN resampling available if we need to end up with a
 * JavaFX image - for as long as setSmooth() does nothing. We can get rid of
 * this once the Prism pipeline is better configurable (i.e. once ImageViews can
 * be resampled with NN, instead of bilinear all the way...).
 */
public class FxBresenham {

	// Note: this class used to reuse the same destination image over and over
	// again, but this leads to the scenario where the desination image is attached
	// by an ImageView to the scene graph, while some worker thread tries to modify
	// the same image... which trashes the JavaFX application thread sooner or later...
	// So for now, this class always returns a brand new image, problem solved.
	// We could try to implement some double buffer approach, but I'm not really
	// sure we should... :|
	private final AdaptiveIntBuffer buffer;

	/**
	 * Creates a new Bresenham resampler.
	 */
	public FxBresenham() {
		buffer = new AdaptiveIntBuffer(2);
	}

	/**
	 * Resamples the given source image.
	 *
	 * @param src the source image.
	 * @param width the width of the destination image.
	 * @param height the height of the destination image.
	 * @return the destination image.
	 */
	public WritableImage zoom(Image src, int width, int height) {
		return FxRescaling.bresenham(
				src,
				new WritableImage(width, height),
				buffer.get(0, src),
				buffer.get(1, width * height)
		);
	}

	/**
	 * Upscales the source image with subpixel precision.
	 *
	 * @see FxRescaling.bresenhamUpsacling
	 * @param src the source image.
	 * @param width the width of the destination image.
	 * @param height the height of the destination image.
	 * @param shiftX number of shifted repeated pixels on the x-axis.
	 * @param restX the rest of the repeated pixels on the x-axis.
	 * @param shiftY number of the shifted repeated pixels on the y-axis.
	 * @param restY the rest of the repeated pixels on the y-axis.
	 * @return
	 */
	public WritableImage zoom(Image src, int width, int height, int shiftX, int restX, int shiftY, int restY) {
		return FxRescaling.bresenhamUpscaling(
				src,
				new WritableImage(width, height),
				shiftX, restX,
				shiftY, restY,
				buffer.get(0, src),
				buffer.get(1, width * height)
		);
	}

}
