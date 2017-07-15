package ch.unifr.diva.dip.core.model;

import ch.unifr.diva.dip.api.components.EditorLayerGroup;
import ch.unifr.diva.dip.api.components.EditorLayerOverlay;
import ch.unifr.diva.dip.api.components.ProcessorContext;
import ch.unifr.diva.dip.api.utils.DipThreadPool;
import ch.unifr.diva.dip.utils.IOUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 * A processor context of a {@code RunnableProcessor}.
 */
public class RunnableProcessorContext implements ProcessorContext {

	protected static final org.slf4j.Logger log = LoggerFactory.getLogger(RunnableProcessorContext.class);

	private final DipThreadPool threadPool;
	private final int pageId;
	private final Path directory;
	private final Path exportDirectory;
	private final Path exportRootDirectory;
	private final Map<String, Object> objects;
	private final EditorLayerGroup layer;
	private final EditorLayerOverlay overlay;

	/**
	 * Creates a new processor context.
	 *
	 * @param threadPool the application wide thread pool/executor service.
	 * @param pageId the ID of the page.
	 * @param directory the directory dedicated for persistent data of the
	 * processor.
	 * @param exportDirectory the directory the processor may (publicly) export
	 * files to.
	 * @param exportRootDirectory the export root directory.
	 * @param objects the persistent map of objects of the processor.
	 * @param layer the processor's editor layer.
	 * @param overlay the processor's editor layer overlay.
	 */
	public RunnableProcessorContext(DipThreadPool threadPool, int pageId, Path directory, Path exportDirectory, Path exportRootDirectory, Map<String, Object> objects, EditorLayerGroup layer, EditorLayerOverlay overlay) {
		this.threadPool = threadPool;
		this.pageId = pageId;
		this.directory = directory;
		this.exportDirectory = exportDirectory;
		this.exportRootDirectory = exportRootDirectory;
		this.objects = objects;
		this.layer = layer;
		this.overlay = overlay;
	}

	@Override
	public DipThreadPool getThreadPool() {
		return threadPool;
	}

	@Override
	public int getPageId() {
		return pageId;
	}

	@Override
	public Path getDirectory() {
		return directory;
	}

	@Override
	public Path getExportDirectoryPath() {
		return exportDirectory;
	}

	@Override
	public Path getExportRootDirectoryPath() {
		return exportRootDirectory;
	}

	@Override
	public boolean deleteExportFile(Path file) {
		if (file == null) {
			return false;
		}
		boolean ret = false;
		try {
			ret = Files.deleteIfExists(file);
		} catch (IOException ex) {
			log.warn("failed to remove export file: {}", file, ex);
		}
		try {
			/*
			 * this removes the page directory in the pages directory, and the
			 * pages directory itself (if empty) of the export root directory.
			 *
			 * ./project.dip
			 * ./project.dip-out/pages/<page>/<export-file>
			 *                     ^     ^ <-- both removed if empty
			 *         ^ <-- the export root directory isn't removed, even if empty
			 */
			IOUtils.deleteDirectoryIfEmpty(directory, exportRootDirectory);
		} catch (IOException ex) {
			log.warn("failed to clean up export directory: {}", directory, ex);
		}
		return ret;
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
		sb.append("{pageId=");
		sb.append(this.pageId);
		sb.append(", directory=");
		sb.append(this.directory);
		sb.append(", export-directory=");
		sb.append(this.exportDirectory);
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
