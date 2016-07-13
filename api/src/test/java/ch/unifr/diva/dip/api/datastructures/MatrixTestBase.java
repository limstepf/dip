package ch.unifr.diva.dip.api.datastructures;

import static org.junit.Assert.assertEquals;

/**
 * Base class to test subclasses of MatrixBase.
 */
public class MatrixTestBase {

	private static final double DOUBLE_DELTA = 1e-15;
	private static final float FLOAT_DELTA = 5.96e-08f;

	// the linear array of the transpose of a matrix in column-major order
	// equals the linear array of the same matrix given in row-major order
	// and vice versa. Returns the transpose.
	public static class TransposeTestDataBase {

		public final int rows;
		public final int columns;

		public TransposeTestDataBase(int rows, int columns) {
			this.rows = rows;
			this.columns = columns;
		}
	}

	private <T extends MatrixBase> T getTranspose(T mat) {
		T transpose = mat.transpose();

		assertEquals(mat.columns, transpose.rows);
		assertEquals(mat.rows, transpose.columns);

		return transpose;
	}

	public FloatMatrix verifyTranspose(FloatMatrix mat, float[] expected) {
		FloatMatrix transpose = getTranspose(mat);

		// verify linear data array directly
		verifyEqualData(transpose.data, expected);

		// verify by public accessor method
		for (int i = 0; i < mat.rows; i++) {
			for (int j = 0; j < mat.columns; j++) {
				assertEquals(mat.get(i, j), transpose.get(j, i), FLOAT_DELTA);
			}
		}

		return transpose;
	}

	public DoubleMatrix verifyTranspose(DoubleMatrix mat, double[] expected) {
		DoubleMatrix transpose = getTranspose(mat);

		// verify linear data array directly
		verifyEqualData(transpose.data, expected);

		// verify by public accessor method
		for (int i = 0; i < mat.rows; i++) {
			for (int j = 0; j < mat.columns; j++) {
				assertEquals(mat.get(i, j), transpose.get(j, i), DOUBLE_DELTA);
			}
		}

		return transpose;
	}

	public void verifyEqualData(double[] a, double[] b) {
		assertEquals(a.length, b.length);

		for (int i = 0; i < a.length; i++) {
			assertEqualDouble(a[i], b[i]);
		}
	}

	public void verifyEqualData(float[] a, float[] b) {
		assertEquals(a.length, b.length);

		for (int i = 0; i < a.length; i++) {
			assertEqualFloat(a[i], b[i]);
		}
	}

	public void assertEqualDouble(double a, double b) {
		assertEquals(b, a, DOUBLE_DELTA);
	}

	public void assertEqualFloat(float a, float b) {
		assertEquals(b, a, FLOAT_DELTA);
	}

}