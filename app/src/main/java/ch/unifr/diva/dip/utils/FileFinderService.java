
package ch.unifr.diva.dip.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

/**
 * The {@code FileFinderService} encapsulates a {@code FileFinderTask} such that
 * the search can be restarted. The service can be reconfigured before started
 * again, e.g. to switch the search directory.
 */
public class FileFinderService extends Service<Void> {

	private volatile Path root;
	private final List<FileFinderTask.FileDescriptor> files;
	private final List<Path> suspects;
	private final FileFinderTask.FinderCallback callback;
	private final StringProperty currentDirectoryProperty;

	public FileFinderService(Path root, FileFinderTask.FinderCallback callback) {
		this(root, new ArrayList<>(), new ArrayList<>(), callback);
	}

	public FileFinderService(Path root, List<Path> suspects, List<FileFinderTask.FileDescriptor> files, FileFinderTask.FinderCallback callback) {
		this.root = root;
		this.suspects = suspects;
		this.files = files;
		this.callback = callback;
		this.currentDirectoryProperty = new SimpleStringProperty();
	}

	public void setRoot(Path root) {
		this.root = root;
	}

	public Path getRoot() {
		return this.root;
	}

	/**
	 * Adds a search query for a file.
	 *
	 * @param file the file we're looking for. The filename (only) defines the
	 * search pattern.
	 * @param checksum checksum of the file we're looking for, or an empty
	 * string to ommit the checksum check.
	 * @return a FileDescriptor describing the query.
	 */
	public FileFinderTask.FileDescriptor addQuery(Path file, String checksum) {
		return addQuery(file.getFileName().toString(), checksum);
	}

	/**
	 * Adds a search query for a file.
	 *
	 * @param pattern pattern to match against filenames.
	 * @param checksum checksum of the file we're looking for, or an empty
	 * string to ommit the checksum check.
	 * @return a FileDescriptor describing the query.
	 */
	public FileFinderTask.FileDescriptor addQuery(String pattern, String checksum) {
		final FileFinderTask.FileDescriptor fd = new FileFinderTask.FileDescriptor(pattern, checksum);
		this.files.add(fd);
		return fd;
	}

	/**
	 * Returns the number of registered search queries.
	 *
	 * @return the number of search queries.
	 */
	public int numQueries() {
		return this.files.size();
	}

	public void clear() {
		clearQueries();
		clearSuspects();
	}

	public void clearQueries() {
		this.files.clear();
	}

	public void clearSuspects() {
		this.suspects.clear();
	}

	/**
	 * Returns a list of directories files have been found in.
	 *
	 * @return a list of directories files have been found in.
	 */
	public List<Path> getSuspects() {
		return this.suspects;
	}

	/**
	 * Adds a directory to the list of suspected directories.
	 *
	 * @param directory path to a directory (or file, in which case its parent
	 * directory will be added as suspect).
	 */
	public void addSuspect(Path directory) {
		if (Files.isRegularFile(directory)) {
			directory = directory.getParent();
		}
		if (!this.suspects.contains(directory)) {
			this.suspects.add(directory);
		}
	}

	/**
	 * Return a {@code ProgressBar} with the progress property already bound.
	 *
	 * @return a new {@code ProgressBar}
	 */
	public ProgressBar getProgressBar() {
		final ProgressBar bar = new ProgressBar();
		bar.progressProperty().bind(this.progressProperty());
		return bar;
	}

	/**
	 * CurrentDirectoryProperty. This is a read-only property that indicates the
	 * current directory the finder is looking for some file.
	 *
	 * @return the CurrentDirectoryProperty.
	 */
	public ReadOnlyStringProperty currentDirectoryProperty() {
		return this.currentDirectoryProperty;
	}

	@Override
	protected Task<Void> createTask() {
		final FileFinderTask task = new FileFinderTask(root, suspects, files, callback);
		this.currentDirectoryProperty.bind(task.currentDirectoryProperty());
		return task;
	}

	@Override
	protected void ready() {
		this.currentDirectoryProperty.unbind();
	}

}
