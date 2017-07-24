package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@code MultiFloats2D} unit tests.
 */
public class MultiFloats2DTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<MultiFloats2D> tm = new TestMarshaller<MultiFloats2D>(MultiFloats2D.class, parent) {
			@Override
			public MultiFloats2D newInstance() {
				return TestUtils.newMultiFloats2D(3, 9, 12);
			}
		};
		tm.test();
	}

	@Test
	public void testCopy() {
		MultiFloats2D mfloats = TestUtils.newMultiFloats2D(3, 11, 10);
		assertEquals("expected number of bands", 3, mfloats.getNumBands());
		assertEquals("expected number of rows", 11, mfloats.getNumRows());
		assertEquals("expected number of columns", 10, mfloats.getNumColumns());
		MultiFloats2D copy = mfloats.copy();
		assertEquals("copy equals original", mfloats, copy);
		copy.get(2).data[5][5] = copy.get(2).data[5][5] * 2;
		assertNotEquals("copy no longer equals original", mfloats, copy);
	}

}
