package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Floats2D unit tests.
 */
public class Floats2DTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<Floats2D> tm = new TestMarshaller<Floats2D>(Floats2D.class, parent) {
			@Override
			public Floats2D newInstance() {
				return TestUtils.newFloats2D(5, 3);
			}
		};
		tm.test();
	}

	@Test
	public void testCopy() {
		final Floats2D floats = TestUtils.newFloats2D(3, 4);
		final Floats2D copy = floats.copy();
		assertEquals("copy equals original", floats, copy);
		copy.data[1][2] = copy.data[1][2] * 2;
		assertNotEquals("copy no longer equals original", floats, copy);
	}

	@Test
	public void testFlatten() {
		final int m = 3;
		final int n = 4;
		final Floats2D floats = TestUtils.newFloats2D(m, n);
		final float sum = TestUtils.sum(floats.data);
		final float[] flat = floats.flatten();
		assertEquals("same number of elements", flat.length, m*n);
		final float fsum = TestUtils.sum(flat);
		assertEquals("same sum of elements", sum, fsum, TestUtils.FLOAT_DELTA);
	}

	@Test
	public void testFlattenWithNull() {
		int m = 3;
		int n = 5;
		final Floats2D floats = new Floats2D(new float[][]{
			TestUtils.newFloats(5),
			TestUtils.newFloats(2), // 3 missing values will be zero
			TestUtils.newFloats(5)
		});
		final float sum = TestUtils.sum(floats.data);
		final float[] flat = floats.flatten();
		assertEquals("expected number of elements", flat.length, m*n);
		final float fsum = TestUtils.sum(flat);
		assertEquals("same sum of elements", sum, fsum, TestUtils.FLOAT_DELTA);
	}

}
