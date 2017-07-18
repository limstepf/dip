package ch.unifr.diva.dip.api.datastructures;

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
		return new Floats2D(newFloats2DArray(m,n));
	}

	public static float[][] newFloats2DArray(int m, int n) {
		final float[][] data = new float[m][n];
		for (int y = 0; y < m; y++) {
			data[y] = newFloats(n);
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
		return new Point2D(Math.random(), Math.random());
	}

}
