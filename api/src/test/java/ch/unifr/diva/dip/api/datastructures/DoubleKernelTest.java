package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.TestUtils;
import java.io.IOException;
import java.util.BitSet;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Double kernel unit tests.
 */
public class DoubleKernelTest extends KernelTestBase {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void eyeTest() {
		for (TestUtils.Shape s : eyes) {
			final DoubleMatrix mat = new DoubleMatrix(s.rows, s.columns).fill(1);
			final DoubleKernel kernel = new DoubleKernel(mat);
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

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<DoubleKernel> tm = new TestMarshaller<DoubleKernel>(DoubleKernel.class, parent) {
			@Override
			public DoubleKernel newInstance() {
				final DoubleMatrix mat = new DoubleMatrix(5, 5).fill(1);
				return new DoubleKernel(mat);
			}
		};
		tm.test();
	}

}
