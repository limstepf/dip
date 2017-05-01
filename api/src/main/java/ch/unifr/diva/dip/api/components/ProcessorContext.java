package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.utils.DipThreadPool;
import java.io.IOException;
import java.nio.file.Files;
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
	 * Returns the ID of the page.
	 *
	 * @return the ID of the page.
	 */
	public int getPageId();

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
	 * Returns the path to the processor's export directory. This directory is
	 * for data that a processor can store publicly (i.e. not as state inside
	 * the dip file).
	 *
	 * @return the path to the processor's export directory. The directory will
	 * be created if it doesn't exist yet.
	 * @throws IOException in case the directory could not be created.
	 */
	default Path getExportDirectory() throws IOException {
		return getExportDirectory(true);
	}

	/**
	 * Returns the path to the processor's export directory. This directory is
	 * for data that a processor can store publicly (i.e. not as state inside
	 * the dip file).
	 *
	 * @param createIfNonexistent creates the directory if nonexistent.
	 * @return the path to the processor's export directory.
	 * @throws IOException in case the directory could not be created.
	 */
	default Path getExportDirectory(boolean createIfNonexistent) throws IOException {
		final Path exportDirectory = getExportDirectoryPath();
		if (createIfNonexistent) {
			Files.createDirectories(exportDirectory);
		}
		return exportDirectory;
	}

	/**
	 * Returns the path to the processor's export directory without creating the
	 * directory if it doesn't exist yet.
	 *
	 * @return the path to the processor's export directory.
	 */
	public Path getExportDirectoryPath();

	/**
	 * Returns the path to the export root directory. The export root directory
	 * is shared by all processors of all pages/pipelines in the project. Use at
	 * your own risk.
	 *
	 * @param createIfNonexistent creates the directory if nonexistent.
	 * @return the path to the processor's export directory.
	 * @throws IOException in case the directory could not be created.
	 */
	default Path getExportRootDirectory(boolean createIfNonexistent) throws IOException {
		final Path exportRootDirectory = getExportRootDirectoryPath();
		if (createIfNonexistent) {
			Files.createDirectories(exportRootDirectory);
		}
		return exportRootDirectory;
	}

	/**
	 * Return the path to the export root directory without creating it if it
	 * doesn't exist yet. The export root directory is shared by all processors
	 * of all pages/pipelines in the project. Use at your own risk.
	 *
	 * @return the path to the export root directory.
	 */
	public Path getExportRootDirectoryPath();

	/**
	 * Removes an export file (if it exists), and in addition cleans up/removes
	 * empty export directories too. Should be called in the {@code reset()}
	 * method of the exporting processor instead of the usual
	 * {@code deleteFile()}.
	 *
	 * @param file the export file.
	 * @return {@code true} if the file got deleted, {@code false} otherwise.
	 */
	public boolean deleteExportFile(Path file);

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
