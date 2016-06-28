package ch.unifr.diva.dip.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * I/O utilities.
 *
 * TODO: this is more of a mixed bag than I/O utilities, eh?
 */
public class IOUtils {

	private IOUtils() {
		/* nope :) */
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

	/**
	 * Shuffles an array of int (Fisher-Yates shuffle).
	 *
	 * @param a an array of int.
	 */
	public static void shuffleArray(int[] a) {
		shuffleArray(a, -1);
	}

	/**
	 * Shuffles an array of int (Fisher-Yates shuffle).
	 *
	 * @param a an array of int.
	 * @param seed a random seed, or a negative number to not set it
	 */
	public static void shuffleArray(int[] a, int seed) {
		final Random r = (seed > -1) ? new Random(seed) : new Random();

		for (int i = a.length - 1; i > 0; i--) {
			int j = r.nextInt(i + 1);
			int m = a[j];
			a[j] = a[i];
			a[i] = m;
		}
	}
}
