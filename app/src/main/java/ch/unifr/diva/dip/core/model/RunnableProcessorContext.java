package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.EditorLayerGroup;
import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.nio.file.Path;
import java.util.Map;

/**
 * A processor context of a {@code RunnableProcessor}.
 */
public class RunnableProcessorContext implements ProcessorContext {

	private final DipThreadPool threadPool;
	private final Path directory;
	private final Map<String, Object> objects;
	private final EditorLayerGroup layer;
	private final EditorLayerOverlay overlay;

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
	public RunnableProcessorContext(DipThreadPool threadPool, Path directory, Map<String, Object> objects, EditorLayerGroup layer, EditorLayerOverlay overlay) {
		this.threadPool = threadPool;
		this.directory = directory;
		this.objects = objects;
		this.layer = layer;
		this.overlay = overlay;
	}

	@Override
	public DipThreadPool getThreadPool() {
		return threadPool;
	}

	@Override
	public Path getDirectory() {
		return directory;
	}

	@Override
	public Map<String, Object> getObjects() {
		return objects;
	}

	@Override
	public EditorLayerGroup getLayer() {
		return layer;
	}

	@Override
	public EditorLayerOverlay getOverlay() {
		return overlay;
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
