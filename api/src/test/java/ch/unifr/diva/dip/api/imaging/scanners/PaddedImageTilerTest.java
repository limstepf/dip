package ch.unifr.diva.dip.api.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * PaddedImagetiler unit tests.
 */
public class PaddedImageTilerTest extends ImageTilerTestBase {

	public final List<Rectangle> paddings = Arrays.asList(
			new Rectangle(0, 0),
			new Rectangle(3, 3),
			new Rectangle(7, 11),
			new Rectangle(128, 128)
	);

	@Test
	public void iteratorTest() {
		for (Rectangle region : regions) {
			for (Rectangle tile : tiles) {
				for (Rectangle padding : paddings) {
					doIteratorTest(region, tile, padding);
				}
			}
		}
	}

	public void doIteratorTest(Rectangle region, Rectangle tileSpec, Rectangle paddingSpec) {
		final BufferedImage image = new BufferedImage(
				region.width,
				region.height,
				BufferedImage.TYPE_BYTE_BINARY
		);
		final PaddedImageTiler tiler = new PaddedImageTiler(
				image,
				tileSpec.width,
				tileSpec.height,
				paddingSpec.width,
				paddingSpec.height
		);

		final int maxPadWidth = tileSpec.width + 2 * paddingSpec.width;
		final int maxPadHeight = tileSpec.height + 2 * paddingSpec.height;

		int pixelCount = 0;
		int tileCount = 0;
		PaddedImageTiler.PaddedTile tile;
		while ((tile = tiler.next()) != null) {
			// full/padded region
			assertTrue("valid start x coordinate", tile.x >= 0);
			assertTrue("valid start y coordinate", tile.y >= 0);
			assertTrue("valid end x coordinate", tile.x + tile.width <= region.width);
			assertTrue("valid end y coordinate", tile.y + tile.height <= region.height);
			assertTrue("valid tile width", tile.width <= maxPadWidth);
			assertTrue("valid tile height", tile.height <= maxPadHeight);

			// writable region
			assertTrue(
					"writable region start x in full region",
					tile.writableRegion.x >= tile.x
			);
			assertTrue(
					"writable region start y in full region",
					tile.writableRegion.y >= tile.y
			);
			assertTrue(
					"writable region end x in full region",
					(tile.writableRegion.x + tile.writableRegion.width) <= (tile.x + tile.width)
			);
			assertTrue(
					"writable region end y in full region",
					(tile.writableRegion.y + tile.writableRegion.height) <= (tile.y + tile.height)
			);
			assertTrue(
					"valid writable end x coordinate",
					tile.writableRegion.x + tile.writableRegion.width <= region.width
			);
			assertTrue(
					"valid writable end y coordinate",
					tile.writableRegion.y + tile.writableRegion.height <= region.height
			);
			assertTrue(
					"valid writable tile width",
					tile.writableRegion.width <= tile.writableRegion.width
			);
			assertTrue(
					"valid writable tile height",
					tile.writableRegion.height <= tile.writableRegion.height
			);

			pixelCount += tile.writableRegion.width * tile.writableRegion.height;
			tileCount++;
		}

		final int expectedWritablePixels = region.width * region.height;
		assertEquals("number of processed pixels", expectedWritablePixels, pixelCount);

		final int expectedTiles = (int) (Math.ceil(region.width / (double) tileSpec.width)
				* Math.ceil(region.height / (double) tileSpec.height));
		assertEquals("number of tiles", expectedTiles, tileCount);

	}

}
