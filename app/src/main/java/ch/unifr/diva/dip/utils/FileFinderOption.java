package ch.unifr.diva.dip.utils;

import java.nio.file.FileVisitOption;
import java.util.EnumSet;
import java.util.Set;

/**
 * File finder options define the {@code FileFinder} visiting method.
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

	/**
	 * The maximum depth.
	 */
	public final int maxDepth;

	/**
	 * The visit options.
	 */
	public final Set<FileVisitOption> visitOption;

	/**
	 * Creates a new file finder option.
	 *
	 * @param maxDepth the maximum depth.
	 */
	FileFinderOption(int maxDepth) {
		this(maxDepth, EnumSet.noneOf(FileVisitOption.class));
	}

	/**
	 * Creates a new file finder option.
	 *
	 * @param maxDepth the maximum depth.
	 * @param visitOption the visit options.
	 */
	FileFinderOption(int maxDepth, Set<FileVisitOption> visitOption) {
		this.maxDepth = maxDepth;
		this.visitOption = visitOption;
	}

}
