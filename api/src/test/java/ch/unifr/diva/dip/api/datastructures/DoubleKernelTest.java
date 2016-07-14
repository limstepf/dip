package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.TestUtils;
import ch.unifr.diva.dip.api.imaging.scanners.Location;
import ch.unifr.diva.dip.api.imaging.scanners.RasterScanner;
import java.util.BitSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Double kernel unit tests.
 */
public class DoubleKernelTest extends KernelTestBase {

	@Test
	public void eyeTest() {
		for (TestUtils.Shape s : eyes) {
			System.out.println("shape> " + s);
			final DoubleMatrix mat = new DoubleMatrix(s.rows, s.columns).fill(1);
			final DoubleKernel kernel = new DoubleKernel(mat);
			final int length = mat.data.length;

			// make sure we hit all coefficients once by the raster
			final BitSet bitSet = new BitSet(length);

			int sum = 0;
			for (Location p : new RasterScanner(kernel.bounds())) {
				final int index = kernel.index(p.col, p.row);
				assertTrue("verfiy valid index", index < length);
				bitSet.set(index);
				sum += kernel.getValueFloat(p.col, p.row);
			}

			assertEquals(s.count, bitSet.cardinality());
			assertEquals(s.count, bitSet.nextClearBit(0));
			assertEquals(s.count, sum);
		}
	}

}
