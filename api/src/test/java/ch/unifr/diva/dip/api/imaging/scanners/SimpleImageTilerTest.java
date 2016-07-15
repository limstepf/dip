package ch.unifr.diva.dip.api.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SimpleImageTiler unit tests.
 */
public class SimpleImageTilerTest {

	public final List<Rectangle> regions = Arrays.asList(
			new Rectangle(128, 256),
			new Rectangle(512, 512),
			new Rectangle(2048, 1033),
			new Rectangle(3841, 4921),
			new Rectangle(6878, 5547)
	);

	public final List<Rectangle> tiles = Arrays.asList(
			new Rectangle(32, 32),
			new Rectangle(64, 32),
			new Rectangle(131, 211),
			new Rectangle(1024, 1024)
	);

	@Test
	public void iteratorTest() {
		for (Rectangle region : regions) {
			for (Rectangle tileSpec : tiles) {
				final BufferedImage image = new BufferedImage(
						region.width,
						region.height,
						BufferedImage.TYPE_BYTE_BINARY
				);
				final SimpleImageTiler tiler = new SimpleImageTiler(image, tileSpec.width, tileSpec.height);

				int pixelCount = 0;
				int tileCount = 0;
				Rectangle tile;
				while ((tile = tiler.next()) != null) {
					assertTrue("valid start x coordinate", tile.x >= 0);
					assertTrue("valid start y coordinate", tile.y >= 0);
					assertTrue("valid end x coordinate", tile.x + tile.width <= region.width);
					assertTrue("valid end y coordinate", tile.y + tile.height <= region.height);
					assertTrue("valid tile width", tile.width <= tileSpec.width);
					assertTrue("valid tile height", tile.height <= tileSpec.height);
					pixelCount += tile.width * tile.height;
					tileCount++;
				}

				final int expectedPixels = region.width * region.height;
				assertEquals("number of processed pixels", expectedPixels, pixelCount);

				final int expectedTiles = (int) (Math.ceil(region.width / (double) tileSpec.width)
						* Math.ceil(region.height / (double) tileSpec.height));
				assertEquals("number of tiles", expectedTiles, tileCount);

			}
		}
	}

}
