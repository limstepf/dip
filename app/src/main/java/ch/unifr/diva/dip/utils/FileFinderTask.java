package ch.unifr.diva.dip.utils;

import ch.unifr.diva.dip.api.utils.FxUtils;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

/**
 * Task to find multiple files, one after the other.
 */
public class FileFinderTask extends Task<Void> {

	private final static FileFinderOption OPTION = FileFinderOption.FOLLOWSYMLINK;
	private final static FileFinderOption SUSPECT_OPTION = FileFinderOption.NONRECURSIVE;
	private final Path root;
	private final List<Path> suspects;
	private final List<FileDescriptor> files;
	private final FinderCallback callback;
	private final StringProperty currentDirectoryProperty;

	/**
	 * Constructs an empty FileFinderTask. That is no search queries are
	 * registered yet. Add them with calls to {@code addQuery()}.
	 *
	 * @param root directory to start the search from.
	 * @param callback finder callback.
	 */
	public FileFinderTask(Path root, FinderCallback callback) {
		this(root, new ArrayList<>(), new ArrayList<>(), callback);
	}

	/**
	 * Constructs a FileFinderTask with search queries already defined.
	 * Additional search queries can be added with calls to {@code addQuery()}.
	 *
	 * @param root directory to start the search from.
	 * @param suspects list of directories that always get checked 1-level deep
	 * before the search from root starts. This list is extended during the
	 * search by directories some file has been found in, and thus it's likely
	 * that other files we're looking for are in there as well.
	 * @param files the search queries.
	 * @param callback finder callback.
	 */
	public FileFinderTask(Path root, List<Path> suspects, List<FileDescriptor> files, FinderCallback callback) {
		this.root = root;
		this.suspects = suspects;
		this.files = files;
		this.currentDirectoryProperty = new SimpleStringProperty();
		this.callback = callback;
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
	public FileDescriptor addQuery(Path file, String checksum) {
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
	public FileDescriptor addQuery(String pattern, String checksum) {
		final FileDescriptor fd = new FileDescriptor(pattern, checksum);
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

	protected void setCurrentDirectory(Path directory) {
		final String cd = directory.toString();
		FxUtils.run(() -> this.currentDirectoryProperty.set(cd));
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
	 * Starts the finder task on a new thread. The search can be cancelled with
	 * a call to {@code cancel()}.
	 *
	 * @return thread of the finder task (already started).
	 */
	public Thread start() {
		final Thread thread = new Thread(this);
		thread.start();
		return thread;
	}

	@Override
	protected Void call() throws Exception {
		for (int i = 0; i < files.size(); i++) {
			updateProgress(i+1, files.size());

			final FileDescriptor fd = files.get(i);
			final FileVisitor visitor = new FileVisitor(fd, this);
			Path file = null;

			// check suspected directories first, 1-level deep only
			for (int j = suspects.size() - 1; j >= 0; j--) {
				final Path suspect = suspects.get(j);

				Files.walkFileTree(
						suspect,
						SUSPECT_OPTION.visitOption,
						SUSPECT_OPTION.maxDepth,
						visitor
				);

				if (visitor.getFile() != null) {
					file = visitor.getFile();
					break;
				}
			}

			// search from root then...
			if (file == null) {
				Files.walkFileTree(
						root,
						OPTION.visitOption,
						OPTION.maxDepth,
						visitor
				);
				file = visitor.getFile();
			}

			if (file == null) {
				if (this.callback.executeOnFxThread()) {
					final FileDescriptor t_fd = fd;
					FxUtils.run(() -> this.callback.onMiss(t_fd));
				} else {
					this.callback.onMiss(fd);
				}
			} else {
				addSuspect(file.getParent());

				if (this.callback.executeOnFxThread()) {
					final FileDescriptor t_fd = fd;
					final Path t_file = file;
					FxUtils.run(() -> this.callback.onHit(t_fd, t_file));
				} else {
					this.callback.onHit(fd, file);
				}
			}

			if (this.isCancelled()) {
				break;
			}
		}

		if (this.callback.executeOnFxThread()) {
			FxUtils.run(() -> this.callback.onFinished());
		} else {
			this.callback.onFinished();
		}

		return null;
	}

	/**
	 * FileVisitor looks for exactly one file defined by a FileDescriptor.
	 */
	private static class FileVisitor extends SimpleFileVisitor<Path> {

		private final FileDescriptor fd;
		private final FileFinderTask task;
		private Path file;

		public FileVisitor(FileDescriptor fd, FileFinderTask task) {
			this.fd = fd;
			this.task = task;
			this.file = null;
		}

		public Path getFile() {
			return file;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			if (isFileWereLookingFor(file)) {
				this.file = file;
				return FileVisitResult.TERMINATE;
			}

			if (task.isCancelled()) {
				return FileVisitResult.TERMINATE;
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(final Path directory, BasicFileAttributes attrs) {
			task.setCurrentDirectory(directory);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException ex) {
			return FileVisitResult.CONTINUE;
		}

		private boolean isFileWereLookingFor(Path file) {
			final Path filename = file.getFileName();

			if (filename == null || !fd.matcher().matches(filename)) {
				return false;
			}

			if (fd.checksum.isEmpty()) {
				return true;
			}

			final String md5;
			try {
				md5 = IOUtils.checksum(file);
			} catch (IOException ex) {
				return false;
			}

			return fd.checksum.equals(md5);
		}
	}

	/**
	 * FileFinderTask callback interface.
	 */
	public interface FinderCallback {

		/**
		 * Whether or not to execute the callbacks on the JavaFX application
		 * thread or not.
		 *
		 * @return if True all callbacks are executed on the JavaFX application
		 * thread, otherwise the background thread of the task is used.
		 */
		default boolean executeOnFxThread() {
			return true;
		}

		/**
		 * Fires if the file has been found.
		 *
		 * @param fd file descriptor describing the file.
		 * @param file found location of the file described by fd.
		 */
		public void onHit(FileDescriptor fd, Path file);

		/**
		 * Fires if a file could not be located.
		 *
		 * @param fd file descriptor describing the file.
		 */
		default void onMiss(FileDescriptor fd) {

		}

		/**
		 * Fires once all searches are done, or the search/task got cancelled.
		 */
		default void onFinished() {

		}
	}

	/**
	 * FileDescriptor describes a file to search for.
	 */
	public static class FileDescriptor {

		public final String pattern;
		public final String checksum;
		private PathMatcher matcher;

		public FileDescriptor(Path file) {
			this(file, "");
		}

		public FileDescriptor(String filename) {
			this(filename, "");
		}

		public FileDescriptor(Path file, String checksum) {
			this(file.getFileName().toString(), checksum);
		}

		public FileDescriptor(String filename, String checksum) {
			this.pattern = filename;
			this.checksum = checksum;
		}

		public PathMatcher matcher() {
			if (this.matcher == null) {
				this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.pattern);
			}
			return this.matcher;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "{"
					+ "pattern=" + this.pattern
					+ ", checksum=" + this.checksum
					+ "}";
		}
	}
}
