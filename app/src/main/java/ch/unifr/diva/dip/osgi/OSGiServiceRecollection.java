package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.utils.XmlUtils;
import ch.unifr.diva.dip.core.model.PresetData;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The OSGi service recollection. Keeps track of already seen OSGi services, in
 * order to be able to tell if we've see a particular version
 * ({@code major.minor} part only) of a service before, or for the first time
 * ever. The latter case may be handled by a {@code NewServiceHandler}.
 *
 * @param <T> type of the service interface.
 */
@XmlRootElement(name = "service-recollection")
@XmlAccessorType(XmlAccessType.NONE)
public class OSGiServiceRecollection<T> {

	/**
	 * New/unseen service handler.
	 *
	 * @param <T> type of the service interface.
	 */
	public interface NewServiceHandler<T> {

		/**
		 * Handle new/unseen service.
		 *
		 * @param service the new/unseen service.
		 */
		public void handle(OSGiService<T> service);

		/**
		 * Handle new/unseen service.
		 *
		 * @param pid the PID of the service.
		 * @param version the version of the service.
		 */
		default void handle(String pid, String version) {
			// no-op/unused by default
		}

	}

	private NewServiceHandler<T> newServiceHandler;

	@XmlElement
	public final HashMap<String, ServiceRecollection> services;

	/**
	 * Creates a new, empty OSGi service recollection.
	 */
	public OSGiServiceRecollection() {
		this(null);
	}

	/**
	 * Creates a new, empty OSGi service recollection.
	 *
	 * @param newServiceHandler callback method for new/unseen services.
	 */
	public OSGiServiceRecollection(NewServiceHandler<T> newServiceHandler) {
		this.services = new HashMap<>();
		this.newServiceHandler = newServiceHandler;
	}

	/**
	 * Sets the new service handler.
	 *
	 * @param newServiceHandler callback method for new/unseen services.
	 */
	public void setNewServiceHandler(NewServiceHandler<T> newServiceHandler) {
		this.newServiceHandler = newServiceHandler;
	}

	/**
	 * Returns the recollection of a service.
	 *
	 * @param pid the PID of the service.
	 * @return the recollection of a service.
	 */
	public ServiceRecollection getRecollection(String pid) {
		if (!services.containsKey(pid)) {
			return null;
		}
		return services.get(pid);
	}

	/**
	 * Adds/registers a service to the recollection.
	 *
	 * @param pid the PID of the service.
	 * @param version the version of the service, truncanated to
	 * {@code major.minor}.
	 * @return {@code true} if this is a new/unseen service, {@code false} if
	 * the service is known/has already been seen.
	 */
	public boolean addService(String pid, String version) {
		final ServiceRecollection recollection = getRecollection(pid);
		if (recollection != null) {
			if (recollection.addVersion(version)) {
				if (newServiceHandler != null) {
					newServiceHandler.handle(pid, version);
				}
				return true;
			}
			return false;
		}
		services.put(pid, new ServiceRecollection(version));
		return true;
	}

	/**
	 * Adds/registers a service to the recollection.
	 *
	 * @param service the service.
	 * @return {@code true} if this is a new/unseen service, {@code false} if
	 * the service is known/has already been seen.
	 */
	public boolean addService(OSGiService<T> service) {
		if (addService(service.pid, PresetData.toPresetVersion(service.version))) {
			if (newServiceHandler != null) {
				newServiceHandler.handle(service);
			}
			return true;
		}
		return false;
	}

	/**
	 * Adds/registers a service collection to the recollection.
	 *
	 * @param collection the service collection.
	 */
	public void addServiceCollection(OSGiServiceCollection<T> collection) {
		boolean isNewService;
		for (OSGiService<T> service : collection.getVersions()) {
			if (addService(service.pid, PresetData.toPresetVersion(service.version))) {
				if (newServiceHandler != null) {
					newServiceHandler.handle(service);
				}
			}
		}
	}

	/**
	 * Checks whether the recollection already knows about some service.
	 *
	 * @param pid the PID of the service.
	 * @param version the version of the service.
	 * @return {@code true} if the (version of the) service is known/has already
	 * been seen, {@code false} if this is a new/unseen (version of the)
	 * service.
	 */
	public boolean knowsService(String pid, String version) {
		final ServiceRecollection recollection = getRecollection(pid);
		if (recollection == null) {
			return false;
		}
		return recollection.knowsVersion(version);
	}

	/**
	 * Reads/unmarshalls OSGiServiceRecollection from an XML file.
	 *
	 * @param <T> type of the service interface.
	 * @param file an XML file with OSGiServiceRecollection.
	 * @return the user settings.
	 * @throws JAXBException in case of unexpected errors during unmarshalling.
	 */
	@SuppressWarnings("unchecked")
	public static <T> OSGiServiceRecollection<T> load(Path file) throws JAXBException {
		return XmlUtils.unmarshal(OSGiServiceRecollection.class, file);
	}

	/**
	 * Saves/marshalls the OSGiServiceRecollection to disk.
	 *
	 * @param file the XML File to store the OSGiServiceRecollection.
	 * @throws JAXBException in case of unexpected errors during marshalling.
	 */
	public void save(Path file) throws JAXBException {
		XmlUtils.marshal(this, file);
	}

	/**
	 * A service recollection.
	 */
	public static class ServiceRecollection {

		@XmlElement
		public final Set<String> versions;

		/**
		 * Creates a new, empty service recollection.
		 */
		public ServiceRecollection() {
			this.versions = new HashSet<>();
		}

		/**
		 * Creates a new service recollection.
		 *
		 * @param versions the initially known versions.
		 */
		public ServiceRecollection(String... versions) {
			this.versions = new HashSet<>();
			for (String version : versions) {
				this.versions.add(version);
			}
		}

		/**
		 * Adds/registers a new version to this service recollection.
		 *
		 * @param version the version, truncanated to {@code major.minor}.
		 * @return {@code true} if this is a new/unseen version of the service,
		 * {@code false} if the version is known/has already been seen.
		 */
		public boolean addVersion(String version) {
			if (versions.contains(version)) {
				return false;
			}
			versions.add(version);
			return true;
		}

		/**
		 * Check whether a version is known/has already been seen.
		 *
		 * @param version the version of the service.
		 * @return {@code true} if the version is known/has already been seen,
		 * {@code false} if this is a new/unseen version.
		 */
		public boolean knowsVersion(String version) {
			return versions.contains(version);
		}

	}

}
