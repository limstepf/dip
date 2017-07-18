package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * FloatMatrix unit tests.
 */
public class FloatMatrixTest extends MatrixTestBase {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	public static class TransposeTestData extends TransposeTestDataBase {

		public final float[] columnMajor;
		public final float[] rowMajor;
		public final float[][] rowMajor2D;

		public TransposeTestData(int rows, int columns, float[] columnMajor, float[] rowMajor, float[][] rowMajor2D) {
			super(rows, columns);
			this.columnMajor = columnMajor;
			this.rowMajor = rowMajor;
			this.rowMajor2D = rowMajor2D;
		}
	}

	@Test
	public void testTranspose() {
		List<TransposeTestData> tests = Arrays.asList(
				new TransposeTestData(
						1, 2,
						new float[]{1, 2},
						new float[]{1, 2},
						new float[][]{
							{1, 2}
						}
				),
				new TransposeTestData(
						2, 2,
						new float[]{1, 3, 2, 4},
						new float[]{1, 2, 3, 4},
						new float[][]{
							{1, 2},
							{3, 4}
						}
				),
				new TransposeTestData(
						3, 2,
						new float[]{1, 3, 5, 2, 4, 6},
						new float[]{1, 2, 3, 4, 5, 6},
						new float[][]{
							{1, 2},
							{3, 4},
							{5, 6}
						}
				)
		);

		for (TransposeTestData t : tests) {
			FloatMatrix a = new FloatMatrix(
					t.rows, t.columns,
					FloatMatrix.Layout.COLUMN_MAJOR_ORDER,
					t.columnMajor
			);
			FloatMatrix aT = verifyTranspose(a, t.rowMajor);

			FloatMatrix b = new FloatMatrix(
					t.rows, t.columns,
					FloatMatrix.Layout.ROW_MAJOR_ORDER,
					t.rowMajor
			);
			FloatMatrix bT = verifyTranspose(b, t.columnMajor);

			verifyEqualData(a.data, bT.data);
			verifyEqualData(b.data, aT.data);

			FloatMatrix b2D = new FloatMatrix(t.rowMajor2D);
			verifyEqualData(b.data, b2D.data);
		}
	}

	@Test
	public void testEmptyTranspose() {
		FloatMatrix a = new FloatMatrix();
		FloatMatrix aT = verifyTranspose(a, new float[]{});
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegal2DConstruction() {
		float[][] data = {
			{1, 2},
			{3, 4, 5}
		};
		FloatMatrix mat = new FloatMatrix(data);
	}

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<FloatMatrix> tm = new TestMarshaller<FloatMatrix>(FloatMatrix.class, parent) {
			@Override
			public FloatMatrix newInstance() {
				return new FloatMatrix(5, 5).fill(1);
			}
		};
		tm.test();
	}

}
