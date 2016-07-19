package ch.unifr.diva.dip.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * ZIP File System. Mounts a zip file as a filesystem, on which any methods of
 * the {@link java.nio.file.FileSystem} and {@link java.nio.file.Path} classes
 * can be performed.
 *
 * <p>
 * Note that you can't use {@code File}s with virtual filesystems. E.g.:
 * <pre>
 * <code>
 * ZipFileSystem zip = ZipFileSystem.open(file);
 * Path p = zip.getPath("/file.xml"); // okay up to here
 * File file = p.toFile(); // this throws UnsupportedOperationException!
 * </code>
 * </pre>
 *
 * In case working with {@code Path}s is not an option, maybe an
 * {@code InputStream} or {@code OutputStream} will do.
 */
public class ZipFileSystem implements AutoCloseable {

	private final FileSystem fs;

	// We can use the "jar:file:" prefix to construct an URI to a zip file
	// (jar files are just zip files with some additional conventions...).
	/**
	 * Opens an existing zip file. Use the static method {@code create()} to
	 * create a new zip file and {@code open()} to open a zip file!
	 *
	 * @param uri an URI pointing to a zip file.
	 * @throws IOException
	 * @see #create(java.io.File)
	 * @see #create(java.nio.file.Path)
	 * @see #create(java.net.URI)
	 * @see #open(java.io.File)
	 * @see #open(java.nio.file.Path)
	 */
	public ZipFileSystem(URI uri) throws IOException {
		this(uri, new HashMap<>());
	}

	/**
	 * Default constructor. Use the static method {@code create()} to create a
	 * new zip file and {@code open()} to open a zip file!
	 *
	 * @param uri URI pointing to an existing zip file.
	 * @param env a map of properties configuring the file system.
	 * @throws IOException
	 * @see #create(java.io.File)
	 * @see #create(java.nio.file.Path)
	 * @see #create(java.net.URI)
	 * @see #open(java.io.File)
	 * @see #open(java.nio.file.Path) 
	 */
	public ZipFileSystem(URI uri, HashMap<String, String> env) throws IOException {
		fs = FileSystems.newFileSystem(uri, env);
	}

	/**
	 * Opens an existing zip file.
	 *
	 * @param file the zip file.
	 * @return a ZipFileSystem.
	 * @throws IOException
	 */
	public static ZipFileSystem open(File file) throws IOException {
		return new ZipFileSystem(toURI(file));
	}

	/**
	 * Opens an existing zip file.
	 *
	 * @param file the zip file.
	 * @return a ZipFileSystem.
	 * @throws IOException
	 */
	public static ZipFileSystem open(Path file) throws IOException {
		return new ZipFileSystem(toURI(file));
	}

	/**
	 * Creates a new zip file (system).
	 *
	 * @param file the zip file.
	 * @return a new ZipFileSystem.
	 * @throws IOException
	 */
	public static ZipFileSystem create(File file) throws IOException {
		return ZipFileSystem.create(toURI(file));
	}

	/**
	 * Creates a new zip file (system).
	 *
	 * @param file the zip file.
	 * @return a new ZipFileSystem.
	 * @throws IOException
	 */
	public static ZipFileSystem create(Path file) throws IOException {
		return ZipFileSystem.create(toURI(file));
	}

	/**
	 * Creates a new zip file (system).
	 *
	 * @param uri URI to the zip file.
	 * @return a new ZipFileSystem.
	 * @throws IOException
	 */
	public static ZipFileSystem create(URI uri) throws IOException {
		final HashMap<String, String> env = new HashMap();
		env.put("create", "true");
		env.put("encoding", "UTF-8");
		return new ZipFileSystem(uri, env);
	}

	/**
	 * Returns a path on the virtual/zip file system.
	 *
	 * @param path a path in the zip file.
	 * @return a path.
	 */
	public Path getPath(String path) {
		return fs.getPath(path);
	}

	/**
	 * Tests whether a file exists.
	 *
	 * @param path path to a file in the zip file.
	 * @return True if the file exists, False otherwise.
	 */
	public boolean exists(String path) {
		Path file = getPath(path);
		return exists(file);
	}

	/**
	 * Tests whether a file exists.
	 *
	 * @param file path to a file in the zip file.
	 * @return True if the file exists, False otherwise.
	 */
	public boolean exists(Path file) {
		return Files.exists(file);
	}

	/**
	 * Returns an input stream on the virtual/zip file system.
	 *
	 * @param path a path in the zip file.
	 * @return an (unbuffered) input stream.
	 * @throws IOException
	 */
	public InputStream getInputStream(String path) throws IOException {
		return getInputStream(getPath(path));
	}

	/**
	 * Returns an input stream on the virtual/zip file system.
	 *
	 * @param file a path in the zip file.
	 * @return an (unbuffered) input stream.
	 * @throws IOException
	 */
	public InputStream getInputStream(Path file) throws IOException {
		return Files.newInputStream(file);
	}

	/**
	 * Returns an output stream on the virtual/zip file system.
	 *
	 * @param path a path in the zip file.
	 * @return an (unbuffered) output stream.
	 * @throws IOException
	 */
	public OutputStream getOutputStream(String path) throws IOException {
		return getOutputStream(getPath(path));
	}

	/**
	 * Returns an output stream on the virtual/zip file system.
	 *
	 * @param file a path in the zip file.
	 * @return an (unbuffered) output stream.
	 * @throws IOException
	 */
	public OutputStream getOutputStream(Path file) throws IOException {
		return Files.newOutputStream(file);
	}

	private static URI toURI(Path file) {
		return URI.create("jar:" + file.toUri());
	}

	private static URI toURI(File file) {
		return URI.create("jar:" + file.toURI());
	}

	/**
	 * Returns the underlying filesystem.
	 *
	 * @return the filesystem.
	 */
	public FileSystem getFileSystem() {
		return fs;
	}

	/**
	 * Tells whether or not this filesystem is open.
	 *
	 * @return True if the filesystem is open, False otherwise.
	 */
	public boolean isOpen() {
		return fs.isOpen();
	}

	@Override
	public void close() throws IOException {
		if (fs != null && fs.isOpen()) {
			fs.close();
		}
	}
}
