package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Floats1D unit tests.
 */
public class Floats1DTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<Floats1D> tm = new TestMarshaller<Floats1D>(Floats1D.class, parent) {
			@Override
			public Floats1D newInstance() {
				return TestUtils.newFloats1D(7);
			}
		};
		tm.test();
	}

	@Test
	public void testCopy() {
		final Floats1D floats = TestUtils.newFloats1D(9);
		final Floats1D copy = floats.copy();
		assertEquals("copy equals original", floats, copy);
		copy.data[7] = copy.data[7] * 2;
		assertNotEquals("copy no longer equals original", floats, copy);
	}

}
