package ch.unifr.diva.dip.utils;

import java.nio.file.FileVisitOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * FinderOptions define the FileFinder visiting method.
 */
public enum FileFinderOption {

	/**
	 * Recursively searches the whole sub-tree. Does not follow symbolic links.
	 */
	RECURSIVE(Integer.MAX_VALUE),
	/**
	 * Does not visit sub-directories, just the files in the given root
	 * directory.
	 */
	NONRECURSIVE(1),
	/**
	 * Recursively searches the whole sub-tree while following symbolic links.
	 * Files might be visited more than once in case of a cycle! So consider
	 * checking with {@code hasCycleDetected()} that everything went down as
	 * expected.
	 */
	FOLLOWSYMLINK(Integer.MAX_VALUE, EnumSet.of(FileVisitOption.FOLLOW_LINKS));

	public final int maxDepth;
	public final Set<FileVisitOption> visitOption;

	FileFinderOption(int maxDepth) {
		this(maxDepth, EnumSet.noneOf(FileVisitOption.class));
	}

	FileFinderOption(int maxDepth, Set<FileVisitOption> visitOption) {
		this.maxDepth = maxDepth;
		this.visitOption = visitOption;
	}
}
