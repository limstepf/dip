package ch.unifr.diva.dip.api.imaging.padders;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * TiledPadder unit tests.
 */
public class TiledPadderTest extends ImagePadderTestBase {

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
				{1, 2, 1, 2, 1, 2},
				{3, 4, 3, 4, 3, 4},
				{1, 2, 1, 2, 1, 2},
				{3, 4, 3, 4, 3, 4},
				{1, 2, 1, 2, 1, 2},
				{3, 4, 3, 4, 3, 4}
			})
	);

	@Test
	public void testPadder() {
		TiledPadder padder = new TiledPadder();
		verifyResults(padder, testImages, expected);
	}

}
