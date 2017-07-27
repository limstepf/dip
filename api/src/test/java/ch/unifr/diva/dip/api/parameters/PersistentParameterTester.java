package ch.unifr.diva.dip.api.parameters;

import ch.unifr.diva.dip.api.datastructures.FxColor;
import java.util.Arrays;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.scene.paint.Color;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Simple persistent parameter tester. Verifies that an initial value, and a
 * number of changed values all come through the parameter and it's view.
 *
 * <p>
 * Note that the JavaFX toolkit has to be initialized for this to work.
 *
 * @param <T> class of the value of the parameter.
 * @param <P> class of the persistent parameter.
 * @param <V> class of the parameter's view.
 */
public abstract class PersistentParameterTester<T, P extends PersistentParameter<T>, V extends PersistentParameter.View<T>> {

	public final P parameter;
	public final V view;
	public final T initial;
	private final InvalidationListener listener;
	private int invalidationCounter;

	/**
	 * Creates a new persistent parameter tester.
	 *
	 * @param parameter the parameter.
	 * @param view the parameter's view.
	 * @param initial the expected initial value.
	 */
	public PersistentParameterTester(P parameter, V view, T initial) {
		this.parameter = parameter;
		this.view = view;
		this.initial = initial;
		this.invalidationCounter = 0;
		this.listener = (c) -> invalidationCounter++;
	}

	/**
	 * A list of test values to be set, read back and checked for equality in
	 * {@code test()}.
	 *
	 * @return a list of test values.
	 */
	public abstract List<T> values();

	/**
	 * Tests the expected initial value, and all test values.
	 */
	public void test() {
		testAssignability();

		assertEquals("initial parameter value", initial, parameter.get());
		assertEquals("initial view value", initial, view.get());

		invalidationCounter = 0;
		parameter.property().addListener(listener);

		final List<T> values = values();
		for (T val : values) {
			parameter.set(val);
			assertEquals("changed parameter value", val, parameter.get());
			assertEquals("changed view value", val, view.get());
			postSetTest(val);
		}

		parameter.property().removeListener(listener);
		assertEquals(
				"expected amount of property invalidations",
				values.size(),
				invalidationCounter
		);
	}

	/**
	 * Optional post set test. Gets called after a value from {@code values()}
	 * has just been set on the parameter.
	 *
	 * @param val the value that just has been set.
	 */
	public void postSetTest(T val) {

	}

	/**
	 * Verifies that a composite parameter gets invalidated if one of its
	 * child-parameter changes.
	 *
	 * @param child the child parameter of this composite parameter.
	 * @param value a new value for the child parameter.
	 */
	public void testComposite(PersistentParameter<?> child, Object value) {
		invalidationCounter = 0;
		parameter.property().addListener(listener);
		child.setRaw(value);
		parameter.property().removeListener(listener);
		assertEquals(
				"parent parameter has changed",
				1,
				invalidationCounter
		);
	}

	/**
	 * Values to test parameter value assignability.
	 *
	 * @return values to test parameter value assignability.
	 */
	public List<Object> assignabilityValues() {
		return Arrays.asList(
				true,
				42,
				42.0,
				42.0f,
				42L,
				null,
				new float[]{42.0f},
				"string",
				new FxColor(Color.ALICEBLUE)
		);
	}

	/**
	 * Tests parameter value assignability.
	 */
	public void testAssignability() {
		assertTrue(
				String.format("%s is assignable", initial.getClass().getName()),
				parameter.isAssignable(initial)
		);

		Class<?> objClass;
		String objClassName;
		for (Object obj : assignabilityValues()) {
			objClass = (obj == null) ? null : obj.getClass();
			objClassName = (objClass == null) ? "null" : objClass.getName();
			if (parameter.getValueClass().equals(objClass)) {
				assertTrue(
						String.format("%s is assignable", objClassName),
						parameter.isAssignable(obj)
				);
			} else {
				assertFalse(
						String.format("%s is not assignable", objClassName),
						parameter.isAssignable(obj)
				);
			}
		}
	}

}
