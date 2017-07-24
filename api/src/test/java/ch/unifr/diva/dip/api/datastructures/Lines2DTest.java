package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@code Lines2D} unit tests.
 */
public class Lines2DTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<Lines2D> tm = new TestMarshaller<Lines2D>(Lines2D.class, parent) {
			@Override
			public Lines2D newInstance() {
				return TestUtils.newLines2D(7);
			}
		};
		tm.test();
	}

	@Test
	public void testIterator() {
		Lines2D lines = TestUtils.newLines2D(5);
		int i = 0;
		for (Line2D line : lines) {
			assertEquals("same line", line, lines.get(i));
			i++;
		}
		assertEquals("correct number of lines/iterations", i, lines.size());
	}

	@Test
	public void testCopy() throws CloneNotSupportedException {
		Lines2D lines = TestUtils.newLines2D(5);
		Lines2D copy = lines.copy();
		assertEquals("copy equals original", lines, copy);

		copy.add(TestUtils.newLine2D());
		assertNotEquals("copy no longer equals original", lines, copy);
	}

}
