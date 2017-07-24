package ch.unifr.diva.dip.api.datastructures;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * {@code StringMatrix} unit tests. Strings are assumed to be math. expressions,
 * and we mostly test conversion to Double-/FloatMatrix.
 */
public class StringMatrixTest extends MatrixTestBase {

	@Rule
	public final TemporaryFolder parent = new TemporaryFolder();

	public static class TestData {

		public final String[][] expressions;
		public final double[] doubles;
		public final float[] floats;

		public TestData(String[][] expressions, double[] doubles, float[] floats) {
			this.expressions = expressions;
			this.doubles = doubles;
			this.floats = floats;
		}
	}

	@Test
	public void conversionTest() {
		List<TestData> tests = Arrays.asList(
				new TestData(
						new String[][]{
							{"3", "4.5", "255/2"},
							{"pi", "e", "illegal"}
						},
						new double[]{
							3, 4.5, 255.0 / 2.0, Math.PI, Math.E, Double.NaN
						},
						new float[]{
							3, 4.5f, 255.0f / 2.0f, (float) Math.PI, (float) Math.E, Float.NaN
						}
				),
				new TestData(
						new String[][]{
							{"sin(2*pi) - cos(e)"},
							{"pow(2,31)"}
						},
						new double[]{
							Math.sin(2.0 * Math.PI) - Math.cos(Math.E),
							Math.pow(2, 31)
						},
						new float[]{
							(float) (Math.sin(2.0 * Math.PI) - Math.cos(Math.E)),
							(float) Math.pow(2, 31)
						}
				)
		);

		for (TestData t : tests) {
			StringMatrix mat = new StringMatrix(t.expressions);

			DoubleMatrix matD = mat.getDoubleMatrix();
			verifyEqualData(matD.data, t.doubles);

			FloatMatrix matF = mat.transpose().getFloatMatrix();
			verifyEqualData(matF.transpose().data, t.floats);
		}
	}

	@Test
	public void testMarshaller() throws IOException, JAXBException {
		TestMarshaller<StringMatrix> tm = new TestMarshaller<StringMatrix>(StringMatrix.class, parent) {
			@Override
			public StringMatrix newInstance() {
				return new StringMatrix(3, 2).fill("val");
			}
		};
		tm.test();
	}

}
