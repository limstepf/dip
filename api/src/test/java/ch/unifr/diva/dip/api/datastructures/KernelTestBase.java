package ch.unifr.diva.dip.api.datastructures;

import ch.unifr.diva.dip.api.TestUtils.Shape;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for kernel tests.
 */
public class KernelTestBase {

	public final List<Shape> eyes = Arrays.asList(
			new Shape(1, 1),
			new Shape(1, 3),
			new Shape(3, 1),
			new Shape(3, 3),
			new Shape(4, 2),
			new Shape(5, 5),
			new Shape(6, 3),
			new Shape(7, 7)
	);

}
