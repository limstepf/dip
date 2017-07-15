package ch.unifr.diva.dip.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@code FileVisitor} finds all files that match a specified pattern.
 * Example:
 *
 * <pre><code>
 * FileFinder finder = new FileFinder("*.java");
 * finder.walkFileTree(path);
 * List&lt;Path&gt; matches = finder.getMatches();
 * </code></pre>
 *
 * @see
 * <a href="https://docs.oracle.com/javase/tutorial/essential/io/walk.html">https://docs.oracle.com/javase/tutorial/essential/io/walk.html</a>
 * @see
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-">https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-</a>
 */
public class FileFinder extends SimpleFileVisitor<Path> {

	private static final Logger log = LoggerFactory.getLogger(FileFinder.class);
	private final List<Path> matches = new ArrayList<>();
	private final PathMatcher matcher;
	private boolean cycleIsDetected = false;

	/**
	 * Creates a default {@code FileFinder} that recursively search the whole
	 * subtree.
	 *
	 * @param pattern a glob pattern (e.g. "*.java")
	 */
	public FileFinder(String pattern) {
		this(pattern, "glob");
	}

	/**
	 * Creates a {@code FileFinder}.
	 *
	 * @param pattern a glob pattern (e.g. "*.java")
	 * @param syntax the syntax of the matcher (e.g. "glob").
	 */
	public FileFinder(String pattern, String syntax) {
		this.matcher = FileSystems.getDefault().getPathMatcher(syntax + ":" + pattern);
	}

	/**
	 * Returns all matches.
	 *
	 * @return all files matching the given pattern.
	 */
	public List<Path> getMatches() {
		return matches;
	}

	/**
	 * Returns all matches. Same as {@code getMatches()} but returns a list of
	 * {@code File}s instead of {@code Path}s.
	 *
	 * @return all files matching the given pattern.
	 */
	public List<File> getFiles() {
		return matches.stream().map(e -> e.toFile()).collect(Collectors.toList());
	}

	/**
	 * Returns the number of matches.
	 *
	 * @return the number of matches.
	 */
	public int getNumMatches() {
		return matches.size();
	}

	/**
	 * Returns {@code true} if a cycle has been detected. This can only happen
	 * while following symbolic links. If it has happend, however, the results
	 * can't be trusted since files might have been visited more than once
	 * (twice at most).
	 *
	 * @return {@code true} if a cycle has been detected, {@code false}
	 * otherwise.
	 */
	public boolean hasCycleDetected() {
		return cycleIsDetected;
	}

	/**
	 * Recursively visits all file in the whole sub-tree.
	 *
	 * @param root the initial/root directory
	 * @throws IOException in case of an I/O error.
	 */
	public void walkFileTree(File root) throws IOException {
		walkFileTree(root.toPath());
	}

	/**
	 * Recursively visits all file in the whole sub-tree.
	 *
	 * @param root the initial/root directory
	 * @throws IOException in case of an I/O error.
	 */
	public void walkFileTree(Path root) throws IOException {
		walkFileTree(root, FileFinderOption.RECURSIVE);
	}

	/**
	 * Visits files according to the given FinderOption.
	 *
	 * @param root the initial/root directory
	 * @param option a FinderOption
	 * @throws IOException in case of an I/O error.
	 */
	public void walkFileTree(File root, FileFinderOption option) throws IOException {
		walkFileTree(root.toPath(), option);
	}

	/**
	 * Visits files according to the given FinderOption.
	 *
	 * @param root the initial/root directory
	 * @param option a FinderOption
	 * @throws IOException in case of an I/O error.
	 */
	public void walkFileTree(Path root, FileFinderOption option) throws IOException {
		Files.walkFileTree(root, option.visitOption, option.maxDepth, this);
	}

	private void find(Path file) {
		final Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			matches.add(file);
		}
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		find(file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
		find(directory);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException ex) {
		if (ex instanceof FileSystemLoopException) {
			cycleIsDetected = true;
			log.warn("FileVisitor detected a cycle following a symbolic link: ", ex);
		} else {
			log.error("unexpected FileVisitor error: ", ex);
		}
		return FileVisitResult.CONTINUE;
	}

	/**
	 * Recursively deletes a directory.
	 *
	 * @param directory directory to be removed.
	 * @throws IOException in case of an I/O error.
	 */
	public static void deleteDirectory(Path directory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path directory, IOException ex) throws IOException {
				Files.delete(directory);
				return FileVisitResult.CONTINUE;
			}
		});
	}

}
