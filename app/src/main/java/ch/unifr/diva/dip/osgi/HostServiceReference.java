package ch.unifr.diva.dip.osgi;

import java.util.Objects;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

/**
 * A host service reference. Mock reference for host services to play nice with
 * the rest of the OSGi framework.
 *
 * @param <T> class of the service interface of the tracked services.
 */
public class HostServiceReference<T> implements ServiceReference<T> {

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
		if (!(reference instanceof HostServiceReference)) {
			return -1;
		}
		final HostServiceReference<?> other = (HostServiceReference) reference;
		return this.pid.compareTo(other.pid);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof HostServiceReference)) {
			return false;
		}
		final HostServiceReference<?> other = (HostServiceReference) obj;
		return this.pid.equals(other.pid);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + Objects.hashCode(this.pid);
		return hash;
	}

}
