package ch.unifr.diva.dip.utils;

import static ch.unifr.diva.dip.utils.IOUtils.getRealDirectories;
import static ch.unifr.diva.dip.utils.IOUtils.getRealDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * IOUtils unit tests.
 */
public class IOUtilsTest {

	@Rule
	public TemporaryFolder rootTmpFolder = new TemporaryFolder();
	public File rootFile = null;
	Path root = null;

	@Before
	public void init() throws IOException {
		rootFile = rootTmpFolder.newFile("x.txt");
		root = rootFile.toPath().getParent();
	}

	@Test
	public void testGetRealDirectory() throws IOException {
		Path p = root.resolve("some-new-directory");
		assertFalse("directory does not exist yet", Files.exists(p));

		Path dir = getRealDirectory(p);
		assertTrue("directory exists", Files.exists(dir));
		assertTrue("directory (still) exists", Files.exists(getRealDirectory(p)));
	}

	@Test
	public void testGetRealDirectories() throws IOException {
		Path p = root.resolve("d1/d2/d3/");
		assertFalse("directory does not exist yet", Files.exists(p));

		Path dir = getRealDirectories(p);
		assertTrue("directory exists", Files.exists(dir));
		assertTrue("directory (still) exists", Files.exists(getRealDirectories(p)));
	}

	/**
	 * Test of getFileExtension method, of class IOUtils.
	 */
	@Test
	public void testGetFileExtension() {
		List<FileExtCandidate> candidates = Arrays.asList(
				new FileExtCandidate("", "C:\\imagepng"),
				new FileExtCandidate("", ".png"),
				new FileExtCandidate("png", "image.png"),
				new FileExtCandidate("png", "image.PNG"),
				new FileExtCandidate("png", "imagE.PNg"),
				new FileExtCandidate("png", ".image.png"),
				new FileExtCandidate("png", "C:\\image.png"),
				new FileExtCandidate("png", "C:\\directory\\image.png"),
				new FileExtCandidate("png", "C:\\dot.directory\\image.png"),
				new FileExtCandidate("jpeg", "C:\\image.jpeg"),
				new FileExtCandidate("gz", "C:\\image.tar.gz")
		);

		for (FileExtCandidate c : candidates) {
			assertEquals(c.extension, IOUtils.getFileExtension(c.file));
		}
	}

	public static class FileExtCandidate {

		public final String extension;
		public final String file;

		public FileExtCandidate(String extension, String file) {
			this.extension = extension;
			this.file = file;
		}
	}

	@Test
	public void hashTestBasic() {
		final int M = 256;
		final int max = M * 3;

		for (int i = 0; i < max; i++) {
			String key = String.format("%d", i);
			int h = IOUtils.hash(key, M);
			assertTrue("positive hash", h >= 0);
			assertTrue("upper bound/number of buckets", h < M);
		}
	}

}
