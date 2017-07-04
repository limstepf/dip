package ch.unifr.diva.dip.awt.imaging.padders;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * ZeroPadder unit tests.
 */
public class ZeroPadderTest extends ImagePadderTestBase {

	public final List<PaddedResult> expected = Arrays.asList(
			// 1
			new PaddedResult(new byte[][]{
				{0, 0, 0},
				{0, 1, 0},
				{0, 0, 0}
			}),
			// 1, 2
			// 3, 4
			new PaddedResult(new byte[][]{
				{0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0},
				{0, 0, 1, 2, 0, 0},
				{0, 0, 3, 4, 0, 0},
				{0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0}
			})
	);

	@Test
	public void testPadder() {
		ZeroPadder padder = new ZeroPadder();
		verifyResults(padder, testImages, expected);
	}

}
