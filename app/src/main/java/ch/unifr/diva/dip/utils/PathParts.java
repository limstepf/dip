package ch.unifr.diva.dip.utils;

import java.nio.file.Path;

/**
 * Wrapper class for {@code Path}s to make the filename (without extension) and
 * extension of a file easily accessible.
 */
public class PathParts {

	/**
	 * The wrapped file.
	 */
	final public Path file;

	/**
	 * The name of the file (without extension).
	 */
	final public String name;

	/**
	 * The file extension, or an empty string.
	 */
	final public String extension;

	/**
	 * Creates a new path parts wrapper.
	 *
	 * @param file the file.
	 */
	public PathParts(Path file) {
		this.file = file;
		final String filename = file.getFileName().toString();
		final int idx = filename.lastIndexOf(".");
		if (idx < 0) {
			this.name = filename;
			this.extension = "";
		} else {
			this.name = filename.substring(0, idx);
			this.extension = filename.substring(idx + 1, filename.length());
		}
	}

	/**
	 * Returns the path to a sibling file.
	 *
	 * @param postfix The postfix is inserted after the name, and before a dot
	 * and the extension (if present).
	 * @return a path to a sibling file.
	 */
	public Path getSibling(String postfix) {
		final Path parent = file.getParent();
		if (parent == null) {
			return null;
		}
		if (extension.isEmpty()) {
			return parent.resolve(name + postfix);
		}
		return parent.resolve(name + postfix + "." + extension);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "@"
				+ Integer.toHexString(hashCode())
				+ "{file="
				+ file
				+ ", name="
				+ name
				+ ", ext="
				+ extension
				+ "}";
	}

}
