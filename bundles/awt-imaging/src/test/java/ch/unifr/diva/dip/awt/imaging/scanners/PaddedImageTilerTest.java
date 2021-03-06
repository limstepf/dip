package ch.unifr.diva.dip.awt.imaging.scanners;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * PaddedImagetiler unit tests.
 */
public class PaddedImageTilerTest extends ImageTilerTestBase {

	// this just takes too long for a stupid unit test, so we're skipping the
	// BitSet test with too large test regions.
	public static int maxPixelCountForBitSet = 9999999;
//	public static int maxPixelCountForBitSet = Integer.MAX_VALUE;

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

		final int expectedWritablePixels = region.width * region.height;
		final BitSet pixels;
		if (maxPixelCountForBitSet > expectedWritablePixels) {
			pixels = new BitSet(expectedWritablePixels);
		} else {
			pixels = null;
		}

		int pixelCount = 0;
		int tileCount = 0;
		PaddedImageTiler.PaddedTile tile;
		while ((tile = tiler.next()) != null) {
			// full/padded region
			assertTrue("valid start x coordinate", tile.x >= 0);
			assertTrue("valid start y coordinate", tile.y >= 0);
			assertTrue("valid end x coordinate", tile.x + tile.width <= region.width);
			assertTrue("valid end y coordinate", tile.y + tile.height <= region.height);

			// writable region
			assertTrue(
					"writable region end x in full region",
					(tile.x + tile.writableRegion.x + tile.writableRegion.width) <= (tile.x + tile.width)
			);
			assertTrue(
					"writable region end y in full region",
					(tile.y + tile.writableRegion.y + tile.writableRegion.height) <= (tile.y + tile.height)
			);
			assertTrue(
					"valid writable end x coordinate",
					tile.x + tile.writableRegion.x + tile.writableRegion.width <= region.width
			);
			assertTrue(
					"valid writable end y coordinate",
					tile.y + tile.writableRegion.y + tile.writableRegion.height <= region.height
			);
			assertTrue(
					"valid writable tile width",
					tile.writableRegion.width <= tile.width
			);
			assertTrue(
					"valid writable tile height",
					tile.writableRegion.height <= tile.height
			);

			if (pixels != null) {
				for (Location pt : new RasterScanner(tile.writableRegion, 1)) {
					// tile.x and tile.y are global,
					// pt.row and pt.col are local to the subimage/tile
					pixels.flip(
							rowMajorIndex(
									tile.y + pt.row,
									tile.x + pt.col,
									image.getWidth()
							)
					);
				}
			}

			pixelCount += tile.writableRegion.width * tile.writableRegion.height;
			tileCount++;
		}

		// straight pixel count
		assertEquals("number of processed pixels", expectedWritablePixels, pixelCount);

		// evaluation of bitset representing the image raster
		if (pixels != null) {
			assertEquals("number of written pixels", expectedWritablePixels, pixels.cardinality());
			assertEquals("only written within bounds", expectedWritablePixels, pixels.nextClearBit(0));
		}

		// proper tile count
		final int expectedTiles = getTilesOnAxis(region.width, tileSpec.width)
				* getTilesOnAxis(region.height, tileSpec.height);
		assertEquals("number of tiles", expectedTiles, tileCount);

	}

	// returns the row-major index
	public int rowMajorIndex(int row, int column, int columns) {
		return row * columns + column;
	}

}
