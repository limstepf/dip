package ch.unifr.diva.dip.playground;

import org.junit.Test;

/**
 * Demonstration of plain and simple Java tests. Need to quickly screw around
 * with some Java things? This shows how to. However: do not commit this file if
 * you're screwing around in here; just create a new file in here (i.e. the
 * playground) which will be ignored by git by default.
 *
 * <ul>
 * <li>If JavaFX components are involved, you might have to manually launch the
 * JavaFX toolkit by calling {@code FxUtils.initToolkit();}</li>
 * </ul>
 *
 * <p>
 * These tests are supposed to be run individually/manually. <br />
 * Usage:
 *
 * <ul>
 * <li>In NetBeans just hit "run focused test method" in the context menu (right
 * mouse click).</li>
 * <li>In IntelliJ select the test-method name (e.g. in the "Run" widget) and
 * hit run in the context menu (right mouse click).</li>
 * </ul>
 */
public class JavaTests {

	/**
	 * Launch the demo method.
	 */
	@Test
	public void launchDemoApplication() {
		System.out.println("Byte.MAX_VALUE: \t" + Byte.MAX_VALUE);
		System.out.println("Short.MAX_VALUE: \t" + Short.MAX_VALUE);
		System.out.println("Integer.MAX_VALUE: \t" + Integer.MAX_VALUE);
		System.out.println("Long.MAX_VALUE: \t" + Long.MAX_VALUE);
		System.out.println("Float.MAX_VALUE: \t" + Float.MAX_VALUE);
		System.out.println("Double.MAX_VALUE: \t" + Double.MAX_VALUE);
	}
}
