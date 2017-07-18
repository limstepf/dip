package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Points2D unit tests.
 */
public class Points2DTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<Points2D> tm = new TestMarshaller<Points2D>(Points2D.class, parent) {
			@Override
			public Points2D newInstance() {
				return TestUtils.newPoints2D(11);
			}
		};
		tm.test();
	}

	@Test
	public void testIterator() {
		Points2D points = TestUtils.newPoints2D(6);
		int i = 0;
		for (Point2D point : points) {
			assertEquals("same line", point, points.get(i));
			i++;
		}
		assertEquals("correct number of lines/iterations", i, points.size());
	}

	@Test
	public void testCopy() throws CloneNotSupportedException {
		Points2D points = TestUtils.newPoints2D(5);
		Points2D copy = points.copy();
		assertEquals("copy equals original", points, copy);

		copy.add(TestUtils.newPoint2D());
		assertNotEquals("copy no longer equals original", points, copy);
	}

}
