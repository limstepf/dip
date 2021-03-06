package ch.unifr.diva.dip.awt.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SimpleImageTiler unit tests.
 */
public class SimpleImageTilerTest extends ImageTilerTestBase {

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
					pixelCount += tile.width * tile.height;
					tileCount++;
				}

				final int expectedPixels = region.width * region.height;
				assertEquals("number of processed pixels", expectedPixels, pixelCount);

				final int expectedTiles = getTilesOnAxis(region.width, tileSpec.width) * getTilesOnAxis(region.height, tileSpec.height);
				assertEquals("number of tiles", expectedTiles, tileCount);

			}
		}
	}

}
