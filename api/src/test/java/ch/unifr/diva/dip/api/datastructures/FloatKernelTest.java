package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.TestUtils.Shape;
import java.util.BitSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Float kernel unit tests.
 */
public class FloatKernelTest extends KernelTestBase {

	@Test
	public void eyeTest() {
		for (Shape s : eyes) {
			final FloatMatrix mat = new FloatMatrix(s.rows, s.columns).fill(1);
			final FloatKernel kernel = new FloatKernel(mat);
			final int length = mat.data.length;

			// make sure we hit all coefficients once by the raster
			final BitSet bitSet = new BitSet(length);

			final int xt = kernel.bounds().x + kernel.bounds().width;
			final int yt = kernel.bounds().y + kernel.bounds().height;
			int sum = 0;
			for (int y = kernel.bounds().y; y < yt; y++) {
				for (int x = kernel.bounds().x; x < xt; x++) {
					final int index = kernel.index(x, y);
					assertTrue("verfiy valid index", index < length);
					bitSet.set(index);
					sum += kernel.getValueFloat(x, y);
				}
			}

			assertEquals(s.count, bitSet.cardinality());
			assertEquals(s.count, bitSet.nextClearBit(0));
			assertEquals(s.count, sum);
		}
	}

}
