package ch.unifr.diva.dip.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * A host service reference. Mock reference for host services to play nice with
 * the rest of the OSGi framework.
 */
public class HostServiceReference implements ServiceReference {

	private final String pid;

	/**
	 * Creates a new host service reference.
	 *
	 * @param pid pid of the host service.
	 */
	public HostServiceReference(String pid) {
		this.pid = pid;
	}

	@Override
	public Object getProperty(String key) {
		if ("service.pid".equals(key)) {
			return pid;
		}
		return null;
	}

	@Override
	public String[] getPropertyKeys() {
		return new String[]{};
	}

	@Override
	public Bundle getBundle() {
		return null;
	}

	@Override
	public Bundle[] getUsingBundles() {
		return new Bundle[]{};
	}

	@Override
	public boolean isAssignableTo(Bundle bundle, String className) {
		return bundle == null;
	}

	@Override
	public int compareTo(Object reference) {
		return -1;
	}

}
