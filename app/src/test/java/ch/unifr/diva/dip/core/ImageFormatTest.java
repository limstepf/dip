
package ch.unifr.diva.dip.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ImageFormat unit tests.
 */
public class ImageFormatTest {

    public ImageFormatTest() {

    }

	@Test
	public void testSupportedImageFormats() {
		List<String> supported = Arrays.asList(
				"bmp", "BMP",
				"gif", "GIF",
				"jpeg", "Jpeg", "jpg", "JPG",
				"png", "PNG"
		);

		for (String ext : supported) {
			assertTrue(
					ext + "is supported",
					ImageFormat.isSupported(ext)
			);
		}

		List<String> notSupported = Arrays.asList(
				"ext", "EXT",
				"doc",
				"xml"
		);

		for (String ext : notSupported) {
			assertFalse(
					ext + "is not supported",
					ImageFormat.isSupported(ext)
			);
		}
	}

	@Test
	public void testGetImageFormat() {
		List<TestImageFile> images = Arrays.asList(
				new TestImageFile(Paths.get("/image.png"), ImageFormat.PNG),
				new TestImageFile(Paths.get("/image.jpeg"), ImageFormat.JPEG),
				new TestImageFile(Paths.get("/image.jpg"), ImageFormat.JPEG)
		);

		for (TestImageFile image : images) {
			assertEquals(image.format, ImageFormat.getImageFormat(image.file));
		}

		assertNotEquals(
				ImageFormat.PNG,
				ImageFormat.getImageFormat(Paths.get("/image.jpg"))
		);
	}

	public static class TestImageFile {
		public final Path file;
		public final ImageFormat format;

		public TestImageFile(Path file, ImageFormat format) {
			this.file = file;
			this.format = format;
		}
	}

}