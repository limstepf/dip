package ch.unifr.diva.dip.divaservices;

import ch.unifr.diva.services.DivaServicesCommunicator;
import ch.unifr.diva.services.returnTypes.DivaServicesResponse;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Playground to mess around with the communicator.
 */
public class Playground {

	private static DivaServicesCommunicator communicator;

	@BeforeClass
	public static void beforeClass() {
		communicator = new DivaServicesCommunicator("http://divaservices.unifr.ch/api/v1/");
	}

	@Before
	public void beforeTest() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSomeMethod() throws IOException {
		System.out.println("communicator> " + communicator);
		final int h = 128;
		final int w = 256;
		final BufferedImage image = newSingleBandImage(
				BufferedImage.TYPE_BYTE_GRAY,
				w,
				newSamples(w * h, 255.0f)
		);
		System.out.println("image> " + image);

		DivaServicesResponse response = communicator.runSauvolaBinarization(image, true);
		System.out.println("response> " + response);
		assertNotNull(response);
		System.out.println("response.images> " + response.getImage());

		/*
		 * this fails with a JSONException, which probably should be handled better by the communicator.
		 * Guess the problem is that:
		 * String url = "http://divaservices.unifr.ch/image/" + md5;
		 * isn't available any longer. Moved?
		 * Fixed.
		 *
		 * new api is at:
		 * http://divaservices.unifr.ch/api/v2/
		 * http://divaservices.unifr.ch/api/v2/binarization/sauvolabinarization/1
		 *
		 * v1 is also available
		 * works. :)
		 */
		ImageIO.write(image, "png", new File("D:\\diva-services-in.png"));
		ImageIO.write(response.getImage(), "png", new File("D:\\diva-services-out.png"));
	}

	@Test
	public void testYetAnotherMethod() throws IOException {
		final BufferedImage image = ImageIO.read(new File("D:\\e-codices_bcuf-L0004_001r_small.jpg"));
		System.out.println("image> " + image);
		final Rectangle rectangle = new Rectangle(
				image.getWidth(),
				image.getHeight()
		);
		System.out.println("rectangle> " + rectangle);
		DivaServicesResponse response = communicator.runHistogramTextLineExtraction(image, rectangle);
		System.out.println("response> " + response);
		assertNotNull(response);

		System.out.println("response.lines> " + response.getHighlighter().getData().size());
	}

	/*
	 * seam carving and ocropy don't seem to be working...
	 */
	@Test
	public void testYetAnotherMethod2() throws IOException {
		final BufferedImage image = ImageIO.read(new File("D:\\e-codices_bcuf-L0004_001r_small.jpg"));
		System.out.println("image> " + image);
		final Rectangle rectangle = new Rectangle(
				image.getWidth(),
				image.getHeight()
		);
		System.out.println("rectangle> " + rectangle);
		DivaServicesResponse response = communicator.runSeamCarvingTextlineExtraction(image, rectangle, false);
		System.out.println("response> " + response);
		assertNotNull(response);

		System.out.println("response.lines> " + response.getHighlighter().getData().size());
	}

	@Test
	public void testYetAnotherMethod3() throws IOException {
		final BufferedImage image = ImageIO.read(new File("D:\\e-codices_bcuf-L0004_001r_small.jpg"));
		System.out.println("image> " + image);
		DivaServicesResponse response = communicator.runOcropyPageSegmentation(image, false);
		System.out.println("response> " + response);
		assertNotNull(response);

		System.out.println("response.lines> " + response.getHighlighter().getData().size());
	}

	@Test
	public void testAnotherMethod() {
		System.out.println("communicator> ");
	}

	public static BufferedImage newSingleBandImage(int type, int width, float[] data) {
		int height = data.length / width;
		BufferedImage image = new BufferedImage(width, height, type);
		image.getRaster().setSamples(0, 0, width, height, 0, data);
		return image;
	}

	public static BufferedImage newMultiBandImage(int type, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, type);
		int length = width * height;
		int[] pixels = new int[length];
		for (int i = 0; i < length; i++) {
			pixels[i] = (int) (Math.random() * Integer.MAX_VALUE) & 0x00ffffff;
		}
		image.setRGB(0, 0, width, height, pixels, 0, width);
		return image;
	}

	public static float[] newSamples(int n, float max) {
		final float[] samples = new float[n];
		for (int i = 0; i < n; i++) {
			if (max == 1.0f) {
				samples[i] = (Math.random() > .5) ? 0 : 1;
			} else {
				samples[i] = (float) Math.random() * max;
			}
		}
		return samples;
	}

	public static float[][] newSamples(int m, int n, float max) {
		final float[][] samples = new float[m][n];
		for (int i = 0; i < m; i++) {
			samples[i] = newSamples(n, max);
		}
		return samples;
	}

}
