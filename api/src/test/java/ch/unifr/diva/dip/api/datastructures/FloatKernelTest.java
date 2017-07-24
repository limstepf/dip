package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import java.util.BitSet;
import javax.xml.bind.JAXBException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * {@code FloatKernel} unit tests.
 */
public class FloatKernelTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void eyeTest() {
		for (TestShape s : TestShape.eyes()) {
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

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<FloatKernel> tm = new TestMarshaller<FloatKernel>(FloatKernel.class, parent) {
			@Override
			public FloatKernel newInstance() {
				final FloatMatrix mat = new FloatMatrix(3, 4).fill(1);
				return new FloatKernel(mat);
			}
		};
		tm.test();
	}

}
