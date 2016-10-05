package ch.unifr.diva.dip.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * An OSGi service. Final and thread-safe object that encapsulates a tracked
 * OSGi service. Such a service is uniquely identified/referred to by PID and
 * version (semantic versioning). Services can also be addressed by PID only (or
 * with partial version; e.g. major version only), usually in order to retrieve
 * the latest available version within the given range.
 *
 * @param <T> type/interface of the service.
 */
public class OSGiService<T> {

	/**
	 * The {@code ServiceReference}. May be used to examine the properties of
	 * the service and to get the service object.
	 */
	public final ServiceReference serviceReference;

	/**
	 * The service object referenced by the service reference.
	 */
	public final T serviceObject;

	/**
	 * The PID of the service.
	 */
	public final String pid;

	/**
	 * The symbolic name of the bundle that offers the service.
	 */
	public final String symbolicBundleName;

	/**
	 * The version of the service. The version of the service is directly
	 * derived from the version of the bundle that offers the service.
	 *
	 * <p>
	 * Version identifiers have four components.
	 * <ol>
	 * <li>Major version. A non-negative integer.</li>
	 * <li>Minor version. A non-negative integer.</li>
	 * <li>Micro version. A non-negative integer.</li>
	 * <li>Qualifier. A text string.</li>
	 * </ol>
	 */
	public final Version version;

	/**
	 * Creates a new OSGi service object.
	 *
	 * @param context the bundle context.
	 * @param serviceReference the service reference.
	 */
	public OSGiService(BundleContext context, ServiceReference serviceReference) {
		this.serviceReference = serviceReference;
		this.serviceObject = (T) context.getService(serviceReference);
		this.pid = (String) serviceReference.getProperty("service.pid");

		final Bundle bundle = serviceReference.getBundle();
		this.symbolicBundleName = bundle.getSymbolicName();
		this.version = bundle.getVersion();
	}

	/**
	 * Creates a new OSGi service object. This method is used to wrap host
	 * services not directly exposed to the OSGi framework.
	 *
	 * @param pid pid of the host service.
	 * @param serviceObject the service object.
	 * @param version the version of the service.
	 */
	public OSGiService(String pid, T serviceObject, Version version) {
		this.serviceReference = new HostServiceReference(pid);
		this.serviceObject = serviceObject;
		this.pid = pid;
		this.symbolicBundleName = "host";
		this.version = version;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "pid=" + pid
				+ ", bundle=" + symbolicBundleName
				+ ", version=" + version
				+ "}";
	}

}
