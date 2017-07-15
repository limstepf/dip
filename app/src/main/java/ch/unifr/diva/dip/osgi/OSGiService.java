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
	 * The component name property.
	 */
	public final static String SERVICE_COMPONENTNAME = "component.name";

	/**
	 * The {@code ServiceReference}. May be used to examine the properties of
	 * the service and to get the service object.
	 */
	public final ServiceReference<T> serviceReference;

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
	 * Creates a new OSGi service object. Make sure to have a non-null
	 * {@code pid} and {@code serviceObject} before using/adding the service;
	 * the service might be misconfigured (in which case checking the
	 * {@code /OSGI-INF/<service>.xml} inside the bundle/jar might be worth a
	 * shot...).
	 *
	 * @param context the bundle context.
	 * @param serviceReference the service reference.
	 * @throws ch.unifr.diva.dip.osgi.OSGiInvalidServiceException in case the
	 * service is invalid/misconfigured.
	 */
	public OSGiService(BundleContext context, ServiceReference<T> serviceReference) throws OSGiInvalidServiceException {
		this.serviceReference = serviceReference;
		this.serviceObject = context.getService(serviceReference);

		if (this.serviceObject == null) {
			throw new OSGiInvalidServiceException(String.format(
					"Invalid/misconfigured OSGi service: invalid service object. Bundle=%s, ServiceReference=%s",
					serviceReference.getBundle(),
					serviceReference
			));
		}

		/*
		 * judging by the Declarative Services Specification in the OSGi Compendium
		 * (Release 6), the sensible thing to do here is to take the component name
		 * as a PID (always added by SCR, see p.324), and ignore any other properties
		 * such as "service.pid" (which used to be added by the no longer needed
		 * maven-scr-plugin).
		 * The name of a component defaults to the canonical name of the class, but
		 * may be overwritten in the @Component(name=PID) annotation.
		 */
		final Object pidProp = serviceReference.getProperty(SERVICE_COMPONENTNAME);
		this.pid = (pidProp == null) ? null : pidProp.toString();
		
		if (this.pid == null || this.pid.isEmpty()) {
			throw new OSGiInvalidServiceException(String.format(
					"Invalid/misconfigured OSGi service: the '%s' property (used as PID) is not set! Bundle=%s, ServiceReference=%s, ServiceObject=%s",
					SERVICE_COMPONENTNAME,
					serviceReference.getBundle(),
					serviceReference,
					this.serviceObject
			));
		}

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
		this.serviceReference = new HostServiceReference<>(pid);
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
