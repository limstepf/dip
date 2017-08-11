package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Various {@code Shape2D} unit tests.
 */
public class Shape2DTest {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	@Test
	public void testRectangles2DMarshaller() throws IOException, JAXBException {
		TestMarshaller<Rectangles2D> tm = new TestMarshaller<Rectangles2D>(Rectangles2D.class, parent) {
			@Override
			public Rectangles2D newInstance() {
				return TestUtils.newRectangles2D(10);
			}
		};
		tm.test();
	}

	@Test
	public void testNestedRectangle2DMarshaller() throws IOException, JAXBException {
		TestMarshaller<NestedRectangle2D> tm = new TestMarshaller<NestedRectangle2D>(NestedRectangle2D.class, parent) {
			@Override
			public NestedRectangle2D newInstance() {
				return TestUtils.newNestedRectangle2D(3);
			}
		};
		tm.test();
	}

	@Test
	public void testNestedRectangle2DCopy() {
		NestedRectangle2D rectangle = TestUtils.newNestedRectangle2D(3);
		NestedRectangle2D copy = rectangle.copy();
		assertEquals("copy equals original", rectangle, copy);

		copy.children.add(TestUtils.newNestedRectangle2D(1));
		assertNotEquals("copy no longer equals original", rectangle, copy);
	}

	@Test
	public void testNestedRectangles2DMarshaller() throws IOException, JAXBException {
		TestMarshaller<NestedRectangles2D> tm = new TestMarshaller<NestedRectangles2D>(NestedRectangles2D.class, parent) {
			@Override
			public NestedRectangles2D newInstance() {
				return TestUtils.newNestedRectangles2D(5, 3);
			}
		};
		tm.test();
	}

	@Test
	public void testShapes2DMarshaller() throws IOException, JAXBException {
		TestMarshaller<Shapes2D> tm = new TestMarshaller<Shapes2D>(Shapes2D.class, parent) {
			@Override
			public Shapes2D newInstance() {
				return TestUtils.newShapes2D(10);
			}
		};
		tm.test();
	}

	@Test
	public void testNestedShape2DMarshaller() throws IOException, JAXBException {
		TestMarshaller<NestedShape2D> tm = new TestMarshaller<NestedShape2D>(NestedShape2D.class, parent) {
			@Override
			public NestedShape2D newInstance() {
				return TestUtils.newNestedShape2D(3);
			}
		};
		tm.test();
	}

	@Test
	public void testNestedShape2DCopy() {
		NestedShape2D shape = TestUtils.newNestedShape2D(3);
		NestedShape2D copy = shape.copy();
		assertEquals("copy equals original", shape, copy);

		copy.children.add(TestUtils.newNestedShape2D(1));
		assertNotEquals("copy no longer equals original", shape, copy);
	}

	@Test
	public void testNestedShapes2DMarshaller() throws IOException, JAXBException {
		TestMarshaller<NestedShapes2D> tm = new TestMarshaller<NestedShapes2D>(NestedShapes2D.class, parent) {
			@Override
			public NestedShapes2D newInstance() {
				return TestUtils.newNestedShapes2D(5, 3);
			}
		};
		tm.test();
	}

}
