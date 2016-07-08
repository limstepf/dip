package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.nio.file.Path;
import java.util.Map;

/**
 * The context of a RunnableProcessor (ProcessorWrappers don't need any). The
 * ProcessorContext gives some pointers as to where a processor can persitently
 * save some data/state.
 */
public class ProcessorContext {

	/**
	 * Application wide thread pool/executor service.
	 */
	public final DipThreadPool threadPool;

	/**
	 * Directory dedicated for persistent data of a processor. Note that this
	 * directory resides in a zip file system, so you can't use {@code File} and
	 * have to use {@code Path}s instead (which should be decorated as
	 * {@code InputStream} and/or {@code OutputStream} respectively).
	 */
	public final Path directory;

	/**
	 * A persistent map of objects. Any marshallable object can be stored in
	 * this map which gets saved and restored automatically.
	 */
	public final Map<String, Object> objects;

	/**
	 * The editor layer dedicated the processor. This layer serves as root layer
	 * for this processor and can be populated by more layers. The composite
	 * pattern is used s.t. a layer is either a layer group (composite), or a
	 * layer pane (leaf). The latter can be populated with anything extending
	 * from a JavaFX Node.
	 *
	 * All methods are save to be used from any thread.
	 */
	public final EditorLayerGroup layer;

	/**
	 * Creates a new processor context.
	 *
	 * @param threadPool the application wide thread pool/executor service.
	 * @param directory the directory dedicated for persistent data of the
	 * processor.
	 * @param objects the persistent map of objects of the processor.
	 * @param layer the processor's editor layer.
	 */
	public ProcessorContext(DipThreadPool threadPool, Path directory, Map<String, Object> objects, EditorLayerGroup layer) {
		this.threadPool = threadPool;
		this.directory = directory;
		this.objects = objects;
		this.layer = layer;
	}

	/**
	 * Checks whether all keys are set on the persistent map of objects.
	 *
	 * @param keys keys that need to be set.
	 * @return True if all keys are set, False otherwise.
	 */
	public boolean hasKeys(String... keys) {
		for (String key : keys) {
			if (!objects.containsKey(key)) {
				return false;
			}
		}
		return true;
	}
}
