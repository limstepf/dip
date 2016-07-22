package ch.unifr.diva.dip.osgi;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * OSGi service monitor tests.
 */
public class ServiceMonitorTest {

	public ServiceMonitorTest() {
	}

	/**
	 * Test HostServiceMonitor. If this fails, chances are the corresponding
	 * package has been moved (hardcoded in HostServiceMonitor due to reflection
	 * and stuff).
	 */
	@Test
	public void testHostServiceMonitor() {
		HostServiceMonitor monitor = new HostServiceMonitor();
		assertNotEquals(
				"At least one host service must have been registered",
				monitor.services().size(),
				0
		);
	}

}
