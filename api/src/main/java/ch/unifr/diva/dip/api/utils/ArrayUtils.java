package ch.unifr.diva.dip.api.utils;

import java.util.Random;

/**
 * Array utilities.
 */
public class ArrayUtils {

	/**
	 * Shuffles an array of int (Fisher-Yates shuffle).
	 *
	 * @param a an array of int.
	 */
	public static void shuffleArray(int[] a) {
		shuffleArray(a, -1);
	}

	/**
	 * Shuffles an array of int (Fisher-Yates shuffle).
	 *
	 * @param a an array of int.
	 * @param seed a random seed, or a negative number to not set it
	 */
	public static void shuffleArray(int[] a, int seed) {
		final Random r = (seed < 0) ? new Random() : new Random(seed);

		for (int i = a.length - 1; i > 0; i--) {
			int j = r.nextInt(i + 1);
			int m = a[j];
			a[j] = a[i];
			a[i] = m;
		}
	}

	/**
	 * Flattens the 2D array to a 1D array. This methods assumes that all rows
	 * have the same size. The number of columns of the first row is used to
	 * determine the number of columns. Missing values from smaller rows will be
	 * zero, values from larger rows will be ignored.
	 *
	 * @param data a 2D array.
	 * @return a 1D array of the size {@code data.length * data[0].length}.
	 */
	public static float[] flatten(float[][] data) {
		final int rows = data.length;
		final int columns = (rows > 0) ? data[0].length : 0;
		final int length = rows * columns;
		final float[] flat = new float[length];
		int pos = 0;
		for (float[] a : data) {
			System.arraycopy(a, 0, flat, pos, a.length);
			pos += columns;
		}
		return flat;
	}

	/**
	 * Flattens the 2D array to a 1D array. This methods assumes that all rows
	 * have the same size. The number of columns of the first row is used to
	 * determine the number of columns. Missing values from smaller rows will be
	 * zero, values from larger rows will be ignored.
	 *
	 * @param data a 2D array.
	 * @return a 1D array of the size {@code data.length * data[0].length}.
	 */
	public static double[] flatten(double[][] data) {
		final int rows = data.length;
		final int columns = (rows > 0) ? data[0].length : 0;
		final int length = rows * columns;
		final double[] flat = new double[length];
		int pos = 0;
		for (double[] a : data) {
			System.arraycopy(a, 0, flat, pos, a.length);
			pos += columns;
		}
		return flat;
	}

}
