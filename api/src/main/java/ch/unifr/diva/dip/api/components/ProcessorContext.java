package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.nio.file.Path;
import java.util.Map;

/**
 * A processor context. A processor context offers restricted access to the DIP
 * environment and current project. Such a context is only needed for
 * {@code RunnableProcessor}, not for mere {@code ProcessorWrapper} living in
 * the pipeline editor.
 */
public interface ProcessorContext {

	/**
	 * Returns the application wide thread pool/executor service.
	 *
	 * @return the application wide thread pool/executor service.
	 */
	public DipThreadPool getThreadPool();

	/**
	 * Returns the directory dedicated for persistent data of a processor. Note
	 * that this directory resides in a zip file system, so you can't use
	 * {@code File} and have to use {@code Path}s instead (using
	 * {@code InputStream} and/or {@code OutputStream} respectively).
	 *
	 * @return the directory dedicated for persistent data of a processor.
	 */
	public Path getDirectory();

	/**
	 * Returns the processor's persistent map of objects. Any marshallable
	 * object can be stored in this map which gets saved and restored
	 * automatically.
	 *
	 * @return the processor's persistent map of objects
	 */
	public Map<String, Object> getObjects();

	/**
	 * Returns the editor layer dedicated the processor. This layer serves as
	 * root layer for this processor and can be populated by more layers. The
	 * composite pattern is used s.t. a layer is either a layer group
	 * (composite), or a layer pane (leaf). The latter can be populated with
	 * anything extending from a JavaFX Node.
	 *
	 * All methods are save to be used from any thread.
	 *
	 * @return the editor layer dedicated the processor.
	 */
	public EditorLayerGroup getLayer();

	/**
	 * Returns the editor layer overlay. Sits on top of the layer group of the
	 * processor, is always bilinearly interpolated (never NN), and comes with
	 * an "infinite" area (gets clipped at some point). Primarily used for the
	 * visualization of tools.
	 *
	 * @return the editor layer overlay.
	 */
	public EditorLayerOverlay getOverlay();

	/**
	 * Checks whether all keys are set on the persistent map of objects.
	 *
	 * @param keys keys that need to be set.
	 * @return True if all keys are set, False otherwise.
	 */
	default boolean hasKeys(String... keys) {
		for (String key : keys) {
			if (!getObjects().containsKey(key)) {
				return false;
			}
		}
		return true;
	}

}
