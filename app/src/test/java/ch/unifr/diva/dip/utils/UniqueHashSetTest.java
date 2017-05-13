package ch.unifr.diva.dip.utils;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * Unique hash set tests.
 */
public class UniqueHashSetTest {

	@Test
	public void testUniqueness() {
		final UniqueHashSet<String> A = new UniqueHashSet<>();
		final UniqueHashSet<String> B = new UniqueHashSet<>();
		final String a = "a";

		assertFalse("unique sets, even if empty", A.equals(B));

		A.add(a);
		assertNotEquals("unique sets", A, B);

		B.add(a);
		assertFalse("unique sets, same elements", A.equals(B));
	}

}
