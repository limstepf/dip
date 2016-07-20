package ch.unifr.diva.dip.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;

/**
 * FileFinder "integration" tests. Clearly, these are unit tests; but they're
 * slow, hence the bunch can go IT.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileFinderIT {

	@Rule
	public TemporaryFolder root = new TemporaryFolder();
	public File rootFile = null;

	@Before
	public void init() throws IOException {
		// let's have a bunch of files...
		rootFile = root.newFile("x.txt");
		root.newFile("r1.jpg");
		root.newFile("r2.jpg");
		root.newFile("r1.java");
		root.newFile("r2.java");
		root.newFile("r3.java");
		File linkTarget = root.newFolder("A");
		root.newFile("A\\a3.jpg");
		root.newFile("A\\a4.java");
		root.newFile("A\\a5.java");
		root.newFolder("A", "EMPTY");
		File linkRoot = root.newFolder("A", "B");
		root.newFile("A\\B\\b4.jpg");
		root.newFile("A\\B\\b5.jpg");
		root.newFile("A\\B\\b6.java");
		root.newFile("A\\B\\findMe.txt");
		// java: 3 on root, 6 in total
		// jpg:  2 on root, 5 in total
		// txt:  1 on root, 2 in total
		// any:            13 in total

		// ...how about some simlink cycle fuckery :D
		Path link = linkRoot.toPath().resolve("symlink");
		Files.createSymbolicLink(link, linkTarget.toPath());
	}

	public FileFinderIT() {
	}

	/**
	 * Testing recursive and non-recursive searches.
	 * @throws java.io.IOException
	 */
	@Test
	public void testFileFinder() throws IOException {

		FileFinder finder;
		Path rootPath = rootFile.toPath().getParent();

		finder = new FileFinder("*.java");
		finder.walkFileTree(rootPath, FileFinderOption.NONRECURSIVE);
		assertEquals(
				"3 .java files w. non-recursive search",
				3, finder.getNumMatches()
		);

		finder = new FileFinder("*.java");
		finder.walkFileTree(rootPath);
		assertEquals(
				"6 .java files w. recursive search",
				6, finder.getNumMatches()
		);

		finder = new FileFinder("*.jpg");
		finder.walkFileTree(rootPath, FileFinderOption.NONRECURSIVE);
		assertEquals(
				"2 .jpg files w. non-recursive search",
				2, finder.getNumMatches()
		);

		finder = new FileFinder("*.jpg");
		finder.walkFileTree(rootPath);
		assertEquals("5 .jpg files w. recursive search",
				5, finder.getNumMatches());

		finder = new FileFinder("findMe.txt");
		finder.walkFileTree(rootPath);
		assertEquals("1 `findMe.txt` file in total w. recursive search",
				1, finder.getNumMatches());

		finder = new FileFinder("*.*");
		finder.walkFileTree(rootPath);
		assertEquals("13 files in total w. recursive search",
				13, finder.getNumMatches());

		// this logs a warning since a cycle will be detected.
		finder.walkFileTree(rootPath, FileFinderOption.FOLLOWSYMLINK);
		assertTrue("cycle detected w. recursive search",
				finder.hasCycleDetected());
	}

	// should run last since we delete the test directory here
	@Test
	public void zTestRecDeleteFinder() throws IOException {

		FileFinder finder;
		Path rootPath = rootFile.toPath().getParent();

		assertTrue("directory exists", Files.exists(rootPath));
		FileFinder.deleteDirectory(rootPath);
		assertFalse("directory doesnt exist any more", Files.exists(rootPath));
	}

}
