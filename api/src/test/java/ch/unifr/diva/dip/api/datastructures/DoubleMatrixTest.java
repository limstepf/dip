package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * DoubleMatrix unit tests.
 */
public class DoubleMatrixTest extends MatrixTestBase {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	public static class TransposeTestData extends TransposeTestDataBase {

		public final double[] columnMajor;
		public final double[] rowMajor;
		public final double[][] rowMajor2D;

		public TransposeTestData(int rows, int columns, double[] columnMajor, double[] rowMajor, double[][] rowMajor2D) {
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
						new double[]{1, 2},
						new double[]{1, 2},
						new double[][]{
							{1, 2}
						}
				),
				new TransposeTestData(
						2, 2,
						new double[]{1, 3, 2, 4},
						new double[]{1, 2, 3, 4},
						new double[][]{
							{1, 2},
							{3, 4}
						}
				),
				new TransposeTestData(
						3, 2,
						new double[]{1, 3, 5, 2, 4, 6},
						new double[]{1, 2, 3, 4, 5, 6},
						new double[][]{
							{1, 2},
							{3, 4},
							{5, 6}
						}
				)
		);

		for (TransposeTestData t : tests) {
			DoubleMatrix a = new DoubleMatrix(
					t.rows, t.columns,
					DoubleMatrix.Layout.COLUMN_MAJOR_ORDER,
					t.columnMajor
			);
			DoubleMatrix aT = verifyTranspose(a, t.rowMajor);

			DoubleMatrix b = new DoubleMatrix(
					t.rows, t.columns,
					DoubleMatrix.Layout.ROW_MAJOR_ORDER,
					t.rowMajor
			);
			DoubleMatrix bT = verifyTranspose(b, t.columnMajor);

			verifyEqualData(a.data, bT.data);
			verifyEqualData(b.data, aT.data);

			DoubleMatrix b2D = new DoubleMatrix(t.rowMajor2D);
			verifyEqualData(b.data, b2D.data);
		}
	}

	@Test
	public void testEmptyTranspose() {
		DoubleMatrix a = new DoubleMatrix();
		DoubleMatrix aT = verifyTranspose(a, new double[]{});
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegal2DConstruction() {
		double[][] data = {
			{1, 2},
			{3, 4, 5},
			{6}
		};
		DoubleMatrix mat = new DoubleMatrix(data);
	}

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<DoubleMatrix> tm = new TestMarshaller<DoubleMatrix>(DoubleMatrix.class, parent) {
			@Override
			public DoubleMatrix newInstance() {
				return new DoubleMatrix(5, 5).fill(1);
			}
		};
		tm.test();
	}

}
