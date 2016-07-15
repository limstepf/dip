package ch.unifr.diva.dip.api.imaging.scanners;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

/**
 * ImageTiler base class for unit tests.
 */
public class ImageTilerTestBase {

	public final List<Rectangle> regions = Arrays.asList(
			new Rectangle(128, 256),
			new Rectangle(512, 512),
			new Rectangle(2048, 1033),
			new Rectangle(3841, 4921),
			new Rectangle(6878, 5547)
	);

	public final List<Rectangle> tiles = Arrays.asList(
			new Rectangle(32, 32),
			new Rectangle(64, 32),
			new Rectangle(131, 211),
			new Rectangle(1024, 1024)
	);

}
