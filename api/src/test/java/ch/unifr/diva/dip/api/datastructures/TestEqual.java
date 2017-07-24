package ch.unifr.diva.dip.api.datastructures;

import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Equality tester for collection data structures used as values for parameters,
 * where distinct object with the same content need to be equal to each other.
 *
 * @param <C> type of the data structure to test.
 */
public abstract class TestEqual<C> {

	public C a;
	public C b;

	/**
	 * Creates a new equality tester for collection data structures.
	 */
	public TestEqual() {

	}

	public void test() {
		this.a = newCollection();
		this.b = newCollection();

		assertEqualCollection(a, b);
		for (Map.Entry<String, Object> e : values().entrySet()) {
			addToCollection(a, e.getKey(), e.getValue());
			assertNonEqualCollection(a, b);

			addToCollection(b, e.getKey(), e.getValue());
			assertEqualCollection(a, b);
		}
	}

	protected void assertEqualCollection(C a, C b) {
		assertEquals("collections are equal", a, b);
		assertEquals("collection hashcodes are equal", a.hashCode(), b.hashCode());
	}

	protected void assertNonEqualCollection(C a, C b) {
		assertNotEquals("collections are not equal", a, b);
	}

	public Map<String, Object> values() {
		return TestUtils.newObjectMap(5);
	}

	abstract public C newCollection();

	abstract public void addToCollection(C collection, String key, Object value);

}
