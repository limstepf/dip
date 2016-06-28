
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
		log.info("start");
//		Dictionary props = new Hashtable();
//		props.put("service.pid", "ch.unifr.diva.ProcessorSource");
//		context.registerService(Processor.class.getName(), new ProcessorSource(), props);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		log.info("stop");
	}

}
