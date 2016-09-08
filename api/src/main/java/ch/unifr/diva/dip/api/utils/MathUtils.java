package ch.unifr.diva.dip.api.utils;

/**
 * Mathematical utility methods.
 */
public class MathUtils {

	private MathUtils() {
		// nope.
	}

	/**
	 * Rounds up to the next power of two. This algorithm is from the "Bit
	 * Twiddling Hacks" by Sean Eron Anderson, which assumes 32-bit integers:
	 * <a href="http://graphics.stanford.edu/~seander/bithacks.html">
	 * http://graphics.stanford.edu/~seander/bithacks.html
	 * </a>. This method is adapted to return 2 for the edge cases 0 and 1 (or a
	 * negative integer for that matter).
	 *
	 * @param value the number to round up to the next power of two.
	 * @return the next power of two with respect to the given number, at least
	 * 2.
	 */
	public static int nextPowerOfTwo(int value) {
		if (value < 2) {
			return 2;
		}

		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		value++;

		return value;
	}

}
