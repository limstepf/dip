package ch.unifr.diva.dip.awt.imaging.padders;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * ReflectivePadder unit tests.
 */
public class ReflectivePadderTest extends ImagePadderTestBase {

	public final List<PaddedResult> expected = Arrays.asList(
			// 1
			new PaddedResult(new byte[][]{
				{1, 1, 1},
				{1, 1, 1},
				{1, 1, 1}
			}),
			// 1, 2
			// 3, 4
			new PaddedResult(new byte[][]{
				{4, 3, 3, 4, 4, 3},
				{2, 1, 1, 2, 2, 1},
				{2, 1, 1, 2, 2, 1},
				{4, 3, 3, 4, 4, 3},
				{4, 3, 3, 4, 4, 3},
				{2, 1, 1, 2, 2, 1}
			})
	);

	@Test
	public void testPadder() {
		ReflectivePadder padder = new ReflectivePadder();
		verifyResults(padder, testImages, expected);
	}

}
