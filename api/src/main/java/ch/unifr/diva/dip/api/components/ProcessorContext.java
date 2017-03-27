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
	 * The editor layer overlay. Sits on top of the layer group of the
	 * processor, is always bilinearly interpolated (never NN), and comes with
	 * an "infinite" area (gets clipped at some point). Primarily used for the
	 * visualization of tools.
	 */
	public final EditorLayerOverlay overlay;

	/**
	 * Creates a new processor context.
	 *
	 * @param threadPool the application wide thread pool/executor service.
	 * @param directory the directory dedicated for persistent data of the
	 * processor.
	 * @param objects the persistent map of objects of the processor.
	 * @param layer the processor's editor layer.
	 * @param overlay the processor's editor layer overlay.
	 */
	public ProcessorContext(DipThreadPool threadPool, Path directory, Map<String, Object> objects, EditorLayerGroup layer, EditorLayerOverlay overlay) {
		this.threadPool = threadPool;
		this.directory = directory;
		this.objects = objects;
		this.layer = layer;
		this.overlay = overlay;
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append('@');
		sb.append(Integer.toHexString(this.hashCode()));
		sb.append("{directory=");
		sb.append(this.directory);
		sb.append(", objects=[");
		final int n = objects.size();
		int i = 0;
		for (Map.Entry<String, Object> e : objects.entrySet()) {
			i++;
			sb.append(e.getKey());
			sb.append('=');
			sb.append(e.getValue());
			if (i < n) {
				sb.append(", ");
			}
		}
		sb.append("], layer=");
		sb.append(this.layer);
		sb.append(", overlay=");
		sb.append(this.overlay);
		sb.append('}');
		return sb.toString();
	}

}
