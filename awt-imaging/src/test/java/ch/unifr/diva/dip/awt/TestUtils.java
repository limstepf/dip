package ch.unifr.diva.dip.awt;

/**
 * Utility classes and methods to help testing.
 */
public class TestUtils {

	/**
	 * A simple shape to test regions, matrices, kernels, and what not.
	 */
	public static class Shape {

		public final int x;
		public final int y;
		public final int rows;
		public final int columns;
		public final int count;

		public Shape(int rows, int columns) {
			this(0, 0, columns, rows);
		}

		public Shape(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.rows = height;
			this.columns = width;
			this.count = this.rows * this.columns;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "{"
					+ "x=" + x
					+ ", y=" + y
					+ ", rows=" + rows
					+ ", columns=" + columns
					+ ", count=" + count
					+ "}";
		}
	}

}
