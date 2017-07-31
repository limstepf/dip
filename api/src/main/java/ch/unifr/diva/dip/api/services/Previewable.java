package ch.unifr.diva.dip.api.services;

import ch.unifr.diva.dip.api.components.ProcessorContext;
import java.awt.Rectangle;
import javafx.scene.image.Image;

/**
 * Previewable interface. Processor extension to offer preview functionality.
 */
public interface Previewable {

	/**
	 * Sets up the preview. Called once initially and each time upon changing
	 * parameters of the processor. Hook-method intended for previews that still
	 * need to process the whole source image, no matter what region currently
	 * is being previewed (e.g. a global threshold), or just to read out and
	 * update the (preview) parameters.
	 *
	 * @param context the processor context.
	 */
	default void previewSetup(ProcessorContext context) {

	}

	/**
	 * Returns the source image that is being modified by the processor.
	 *
	 * @param context the processor context.
	 * @return the source image, or {@code null} if no source image can be
	 * displayed.
	 */
	public Image previewSource(ProcessorContext context);

	/**
	 * Returns the processed preview image. The preview image is a subimage of
	 * the preview source image.
	 *
	 * <p>
	 * <em>Thread-safety:</em> make sure to always return a new {@code Image}
	 * and not to reuse the same one over and over again, since we can't modify
	 * the Image on a background-thread once it has been attached to the JavaFX
	 * scene graph!
	 *
	 * @param context the processor context.
	 * @param bounds the bounds (or region) to be previewed.
	 * @return the processed preview image, or {@code null} if no preview image
	 * can be displayed.
	 */
	public Image preview(ProcessorContext context, Rectangle bounds);

}
