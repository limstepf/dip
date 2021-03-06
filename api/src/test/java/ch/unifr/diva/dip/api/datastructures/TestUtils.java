package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.utils.MathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.Assert.assertTrue;

/**
 * Test utilities.
 */
public class TestUtils {

	public final static float FLOAT_DELTA = 1e-5f;
	public static final double DOUBLE_DELTA = 1e-10;

	private TestUtils() {
		// nope
	}

	public static float sum(float[][] data) {
		float sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += sum(data[i]);
		}
		return sum;
	}

	public static float sum(float[] data) {
		float sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static double sum(double[][] data) {
		double sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += sum(data[i]);
		}
		return sum;
	}

	public static double sum(double[] data) {
		double sum = 0;
		for (int i = 0; i < data.length; i++) {
			sum += data[i];
		}
		return sum;
	}

	public static Floats1D newFloats1D(int n) {
		return new Floats1D(newFloats(n));
	}

	public static Floats2D newFloats2D(int m, int n) {
		return new Floats2D(newFloats2DArray(m, n));
	}

	public static float[][] newFloats2DArray(int m, int n) {
		final float[][] data = new float[m][n];
		for (int y = 0; y < m; y++) {
			data[y] = newFloats(n);
		}
		return data;
	}

	public static MultiFloats2D newMultiFloats2D(int bands, int m, int n) {
		final MultiFloats2D data = new MultiFloats2D();
		for (int i = 0; i < bands; i++) {
			data.add(newFloats2D(m, n));
		}
		return data;
	}

	public static float[] newFloats(int n) {
		final float[] data = new float[n];
		for (int i = 0; i < n; i++) {
			data[i] = (float) Math.random();
		}
		return data;
	}

	public static double[][] newDoubles2DArray(int m, int n) {
		final double[][] data = new double[m][n];
		for (int y = 0; y < m; y++) {
			data[y] = newDoubles(n);
		}
		return data;
	}

	public static double[] newDoubles(int n) {
		final double[] data = new double[n];
		for (int i = 0; i < n; i++) {
			data[i] = Math.random();
		}
		return data;
	}

	public static Lines2D newLines2D(int n) {
		Lines2D lines = new Lines2D();
		for (int i = 0; i < n; i++) {
			lines.add(newLine2D());
		}
		return lines;
	}

	public static Line2D newLine2D() {
		return new Line2D(
				newPoint2D(),
				newPoint2D()
		);
	}

	public static Points2D newPoints2D(int n) {
		Points2D points = new Points2D();
		for (int i = 0; i < n; i++) {
			points.add(newPoint2D());
		}
		return points;
	}

	public static Point2D newPoint2D() {
		return new Point2D(Math.random() * 100, Math.random() * 100);
	}

	public static Circle2D newCircle2D() {
		return new Circle2D(
				Math.random() * 100,
				Math.random() * 100,
				Math.random() * 100
		);
	}

	public static Rectangle2D newRectangle2D() {
		return new Rectangle2D(
				Math.random() * 100,
				Math.random() * 100,
				Math.random() * 100,
				Math.random() * 100
		);
	}

	public static Rectangles2D newRectangles2D(int n) {
		final List<Rectangle2D> rectangles = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			rectangles.add(newRectangle2D());
		}
		return new Rectangles2D(rectangles);
	}

	public static IntegerRectangle newIntegerRectangle() {
		return new IntegerRectangle(
				MathUtils.randomInt(0, 100),
				MathUtils.randomInt(0, 100),
				MathUtils.randomInt(0, 100),
				MathUtils.randomInt(0, 100)
		);
	}

	public static Polyline2D newPolyline2D() {
		Polyline2D poly = new Polyline2D(newPoints2D(5));
		return poly;
	}

	public static Polygon2D newPolygon2D() {
		Polygon2D poly = new Polygon2D(newPoints2D(5));
		return poly;
	}

	public static Shape2D newRandomShape() {
		switch (MathUtils.randomInt(0, 5)) {
			case 1:
				return newCircle2D();
			case 2:
				return newPolyline2D();
			case 3:
				return newPolygon2D();
			case 4:
				return newIntegerRectangle();
			default:
				return newRectangle2D();
		}
	}

	public static Shapes2D newShapes2D(int n) {
		final List<Shape2D> shapes = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			shapes.add(newRandomShape());
		}
		return new Shapes2D(shapes);
	}

	public static NestedRectangle2D newNestedRectangle2D(int depth) {
		final List<NestedRectangle2D> children = new ArrayList<>();
		if (depth > 0) {
			final int n = MathUtils.randomInt(0, 3);
			for (int i = 0; i < n; i++) {
				children.add(newNestedRectangle2D(depth - 1));
			}
		}
		return new NestedRectangle2D(
				Math.random() * 100,
				Math.random() * 100,
				Math.random() * 100,
				Math.random() * 100,
				children
		);
	}

	public static NestedRectangles2D newNestedRectangles2D(int n, int depth) {
		final List<NestedRectangle2D> rectangles = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			rectangles.add(newNestedRectangle2D(depth));
		}
		return new NestedRectangles2D(rectangles);
	}

	public static NestedShape2D newNestedShape2D(int depth) {
		final List<NestedShape2D> children = new ArrayList<>();
		if (depth > 0) {
			final int n = MathUtils.randomInt(2, 5);
			for (int i = 0; i < n; i++) {
				children.add(newNestedShape2D(depth - 1));
			}
		}
		return new NestedShape2D(
				newRandomShape(),
				children
		);
	}

	public static NestedShapes2D newNestedShapes2D(int n, int depth) {
		final List<NestedShape2D> shapes = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			shapes.add(newNestedShape2D(depth));
		}
		return new NestedShapes2D(shapes);
	}

	public static ValueListSelection newValueListSelection(int size, int selection) {
		assertTrue("valid selection", selection < size);
		return new ValueListSelection(newObjectList(size), selection);
	}

	public static ValueList newValueList(int size) {
		return new ValueList(newObjectList(size));
	}

	public static ValueMapSelection newValueMapSelection(int size) {
		final Map<String, Object> map = newObjectMap(size);
		final List<String> keys = new ArrayList<>(map.keySet());
		return new ValueMapSelection(map, keys.get(MathUtils.randomInt(0, size)));
	}

	public static ValueMap newValueMap(int size) {
		return new ValueMap(newObjectMap(size));
	}

	public static Map<String, Object> newObjectMap(int n) {
		final Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < n; i++) {
			map.put(newString(), newObject());
		}
		return map;
	}

	public static List<Object> newObjectList(int n) {
		final List<Object> objects = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			objects.add(newObject());
		}
		return objects;
	}

	public static Object newObject() {
		int random = MathUtils.randomInt(0, 5);
		switch (random) {
			case 0:
				return Math.random() < 0.5;
			case 1:
				return MathUtils.randomInt(0, 128);
			case 2:
				return (float) Math.random();
			case 3:
				return Math.random();
			case 4:
			default:
				return newString();
		}
	}

	public static String newString() {
		return UUID.randomUUID().toString();
	}

}
