
package ch.unifr.diva.dip.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi host activator.
 */
public class OSGiHostActivator implements BundleActivator {

	private static final Logger log = LoggerFactory.getLogger(OSGiHostActivator.class);

	public OSGiHostActivator() {

	}

	@Override
	public void start(BundleContext context) throws Exception {
		log.debug("start");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		log.debug("stop");
	}

}
