package ch.unifr.diva.dip.utils;

import com.rits.cloning.Cloner;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * I/O utilities.
 */
public class IOUtils {

	// cloner is thread safe. One instance can be used by multiple threads
	// at the same time.
	private final static Cloner cloner = new Cloner();

	/**
	 * Platform dependent line separator. This just holds the system property
	 * "line.separator" for convenience, if needed (or just use
	 * {@code println()} a bunch of times...).
	 *
	 * This is a {@code "\n"} (line feed) on a UNIX style machine, and
	 * {@code "\r\n"} (carrige return and line feed) on a Windows box, ...
	 */
	public final static String NL = System.getProperty("line.separator");

	private IOUtils() {
		/* nope :) */
	}

	/**
	 * Deep clones {@code obj}.
	 *
	 * @param <T> the type of {@code obj}.
	 * @param obj the object to be deep-cloned.
	 * @return a deep-clone (or deep-copy) of {@code obj}.
	 */
	public static <T> T deepClone(final T obj) {
		return cloner.deepClone(obj);
	}

	/**
	 * Returns a real path to a directory, creating it first if it doesn't exist
	 * yet.
	 *
	 * @param directory Path to some directory, not necessarily existing yet.
	 * @return Real path to an existing directory.
	 * @throws IOException
	 */
	public static Path getRealDirectory(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return Files.createDirectory(directory);
		} else {
			return directory.toRealPath();
		}
	}

	/**
	 * Returns a real path to a directory, creating non-existing directories
	 * along the way, starting from root.
	 *
	 * @param directory Path to some directory, not necessarily existing yet.
	 * @return Real path to an existing directory.
	 * @throws IOException
	 */
	public static Path getRealDirectories(Path directory) throws IOException {
		final List<Path> directories = new ArrayList<>();
		Path p = directory;
		final Path root = p.getRoot();

		while (p != null && !p.equals(root)) {
			directories.add(p);
			p = p.getParent();
		}

		Path ret = directory;

		final ListIterator<Path> iter = directories.listIterator(directories.size());
		while (iter.hasPrevious()) {
			Path d = iter.previous();
			if (!Files.exists(d)) {
				ret = Files.createDirectory(d);
			} else {
				ret = d;
			}
		}

		return ret.toRealPath();
	}

	/**
	 * Recursively removes directories if they're empty up to the given root
	 * directory. The root directory itself will not be removed, even if empty.
	 *
	 * @param directory the starting directory (a file of root's subtree).
	 * @param root the root directory.
	 * @throws IOException probably in case the directory doesn't exist...
	 */
	public static void deleteDirectoryIfEmpty(Path directory, Path root) throws IOException {
		if (Files.exists(directory) && isDirectoryEmpty(directory)) {
			final Path parent = directory.getParent();
			Files.delete(directory);
			if (parent != null && !parent.equals(root)) {
				deleteDirectoryIfEmpty(parent, root);
			}
		}
	}

	/**
	 * Checks whether the directory is empty, or not.
	 *
	 * @param directory path to the directory.
	 * @return {@code true} if the directory is empty, {@code false} otherwise.
	 * @throws IOException probably in case the directory doesn't exist...
	 */
	public static boolean isDirectoryEmpty(Path directory) throws IOException {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory)) {
			return !ds.iterator().hasNext();
		}
	}

	/**
	 * Returns the file extension of a file.
	 *
	 * @param file a path to some file.
	 * @return the file extension (lowercase) or an empty string.
	 */
	public static String getFileExtension(Path file) {
		return getFileExtension(file.toString());
	}

	/**
	 * Returns the file extension of a file.
	 *
	 * @param file a path to some file.
	 * @return the file extension (lowercase) or an empty string.
	 */
	public static String getFileExtension(File file) {
		return getFileExtension(file.toString());
	}

	/**
	 * Returns the file extension of a file.
	 *
	 * @param file a path or a filename.
	 * @return the file extension (lowercase) or an empty string.
	 */
	public static String getFileExtension(String file) {
		final int i = file.lastIndexOf('.');
		final int j = Math.max(file.lastIndexOf('/'), file.lastIndexOf('\\'));
		if (i > 0 && i > j) {
			return file.substring(i + 1).toLowerCase();
		}

		return "";
	}

	/**
	 * Increments the suffix of a string. Used to get the "next" name for copied
	 * or cloned things. Base case (without suffix): "foo" gets turned into "foo
	 * (2)", and then "foo (2)" gets turned into "foo (3)", and so on.
	 *
	 * <p>
	 * Note that this does not take into account already incremented names. For
	 * example if we have "foo" and a "foo (2)" too, and copy/clone "foo" again,
	 * we'll end up with two "foo (2)".
	 *
	 * @param base the string, possibly already with a suffix.
	 * @return the string with incremented suffix.
	 */
	public static String nameSuffixIncrement(String base) {
		final Pattern p = Pattern.compile("(.*?) \\(((\\d+))\\)");
		final Matcher m = p.matcher(base);
		if (m.find()) {
			final int n = Integer.parseInt(m.group(2)) + 1;
			return String.format(
					"%s (%d)",
					m.group(1),
					n
			);
		}
		return base + " (2)";
	}

	/**
	 * Returns the root element of an XML document.
	 *
	 * @param file path to some file.
	 * @return the root element of an XML document, or {@code null}.
	 */
	public static Element getRootElement(Path file) {
		if (Files.exists(file)) {
			try (InputStream is = Files.newInputStream(file)) {
				return getRootElement(is);
			} catch (IOException ex) {
			}
		}
		return null;
	}

	/**
	 * Returns the root element of an XML document.
	 *
	 * @param is input stream of some file.
	 * @return the root element of an XML document, or {@code null}.
	 */
	public static Element getRootElement(InputStream is) {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document doc = builder.parse(is);
			return doc.getDocumentElement();
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			return null;
		}
	}

	/**
	 * Returns the checksum (MD5) of a file.
	 *
	 * @param file a file.
	 * @return the checksum (MD5).
	 * @throws IOException
	 */
	public static String checksum(Path file) throws IOException {
		try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				try (DigestInputStream dstream = new DigestInputStream(stream, md)) {
					byte[] buffer = new byte[8192];
					while (dstream.read(buffer) > 0) {
						// discard input
					}
				}
				return toHexString(md.digest());
			} catch (NoSuchAlgorithmException ex) {
				// now this should really never happen...
				return "ERR_NO_MD5_IMPLEMENTATION";
			}
		}
	}

	/**
	 * Converts a bunch of bytes to a hex string.
	 *
	 * @param data a bunch of bytes.
	 * @return a hex string.
	 */
	public static String toHexString(byte[] data) {
		final StringBuilder buf = new StringBuilder();

		for (int i = 0; i < data.length; i++) {
			int b = data[i] & 0xFF;
			if (b < 16) {
				buf.append('0');
			}
			buf.append(Integer.toHexString(b));
		}

		return buf.toString();
	}

	/**
	 * Simple, all-purpose hash function. Using a default alphabet size of 64.
	 *
	 * @param key the key to be hashed.
	 * @param M number of buckets, s.t. {@code 0 <= hash < M}. This should be a
	 * prime number.
	 * @return hash value.
	 */
	public static int hash(String key, int M) {
		return hash(key, M, 64);
	}

	/**
	 * Simple, all-purpose hash function.
	 *
	 * @param key the key to be hashed.
	 * @param M number of buckets, s.t. {@code 0 <= hash < M}. This should be a
	 * prime number.
	 * @param A size of the alphabet (e.g. 64). This is not *that* important.
	 * @return hash value.
	 */
	public static int hash(String key, int M, int A) {
		int hash = 0;

		for (int i = 0; i < key.length(); i++) {
			hash = ((A * hash) + key.charAt(i)) % M;
		}

		return hash;
	}

}
