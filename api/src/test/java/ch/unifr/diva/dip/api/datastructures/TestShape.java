package ch.unifr.diva.dip.api.datastructures;

import java.util.Arrays;
import java.util.List;

/**
 * A simple shape to test regions, matrices, kernels, and what not.
 */
public class TestShape {

	public final int x;
	public final int y;
	public final int rows;
	public final int columns;
	public final int count;

	/**
	 * Creates a new test shape.
	 *
	 * @param rows number of rows.
	 * @param columns number of columns.
	 */
	public TestShape(int rows, int columns) {
		this(0, 0, columns, rows);
	}

	/**
	 * Creates a new test shape.
	 *
	 * @param x the X coordinate.
	 * @param y the Y coordinate.
	 * @param width the width.
	 * @param height the height.
	 */
	public TestShape(int x, int y, int width, int height) {
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

	public static List<TestShape> eyes() {
		return Arrays.asList(
				new TestShape(1, 1),
				new TestShape(1, 3),
				new TestShape(3, 1),
				new TestShape(3, 3),
				new TestShape(4, 2),
				new TestShape(5, 5),
				new TestShape(6, 3),
				new TestShape(7, 7)
		);
	}

}
