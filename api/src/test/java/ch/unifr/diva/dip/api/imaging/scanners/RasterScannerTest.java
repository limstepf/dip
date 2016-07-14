package ch.unifr.diva.dip.api.imaging.scanners;

import ch.unifr.diva.dip.api.TestUtils;
import ch.unifr.diva.dip.api.TestUtils.Shape;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Raster scanner unit tests.
 */
public class RasterScannerTest {

	public final List<TestUtils.Shape> regions = Arrays.asList(
			new TestUtils.Shape(0, 0, 1, 1),
			new TestUtils.Shape(-1, -1, 3, 3),
			new TestUtils.Shape(-1, -2, 4, 2),
			new TestUtils.Shape(0, 0, 31, 69),
			new TestUtils.Shape(31, 11, 40, 20),
			new TestUtils.Shape(5, -7, 150, 53),
			new TestUtils.Shape(0, 0, 211, 301)
	);

	@Test
	public void scannerTest() {
		for (Shape s : regions) {
			final Rectangle region = new Rectangle(s.x, s.y, s.columns, s.rows);

			// set each bit for each index to true, then verify all have been
			// set.
			final BitSet bitSet = new BitSet(s.count);

			int[] rowBounds = new int[]{
				region.y, // inclusive
				region.y + region.height // exclusive
			};

			int[] columnBounds = new int[]{
				region.x, // inclusive
				region.x + region.width // exclusive
			};

			for (Location p : new RasterScanner(region)) {
				bitSet.set(p.index);

				assertTrue("lower row bound", p.row >= rowBounds[0]);
				assertTrue("upper row bound", p.row < rowBounds[1]);

				assertTrue("lower column bound", p.col >= columnBounds[0]);
				assertTrue("upper column bound", p.col < columnBounds[1]);
			}

			assertEquals(s.count, bitSet.cardinality());
			assertEquals(s.count, bitSet.nextClearBit(0));
		}
	}

}
