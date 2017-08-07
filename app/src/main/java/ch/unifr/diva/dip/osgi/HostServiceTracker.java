package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.utils.ReflectionUtils;
import ch.unifr.diva.dip.core.services.api.HostService;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Host Service Tracker. Thread-safe. Host services aren't actually tracked, but
 * just lookup once, since they're not really part of the dynamic OSGi framework
 * but rather published to it (again, once).
 *
 * @param <T> class of the service interface of the tracked services.
 */
public class HostServiceTracker<T> {

	private static final Logger log = LoggerFactory.getLogger(HostServiceTracker.class);
	private final OSGiServiceRecollection<T> osgiProcessorRecollection;
	private final Map<String, OSGiServiceCollection<T>> services;

	/**
	 * Creates a new host service tracker.
	 *
	 * @param osgiProcessorRecollection the OSGi {@code Processor} service
	 * recollection.
	 */
	public HostServiceTracker(OSGiServiceRecollection<T> osgiProcessorRecollection) {
		this.osgiProcessorRecollection = osgiProcessorRecollection;
		this.services = new HashMap<>();

		String pid = "-1";
		try {
			List<String> classes = ReflectionUtils.findClasses(HostService.PACKAGE);
			for (String cn : classes) {
				pid = cn;
				final Class<?> clazz = ReflectionUtils.getClass(cn);
				@SuppressWarnings("unchecked")
				final T serviceObject = (T) clazz.newInstance();
				final OSGiService<T> service = new OSGiService<>(
						pid,
						serviceObject,
						HostService.VERSION
				);
				final OSGiServiceCollection<T> collection = new OSGiServiceCollection<>(
						service
				);
				services.put(pid, collection);
				osgiProcessorRecollection.addService(service);
			}
		} catch (IOException ex) {
			log.error("can't find package: {}", HostService.PACKAGE);
		} catch (InstantiationException | IllegalAccessException ex) {
			log.error("invalid host service: {}", pid);
		}
	}

	/**
	 * Returns an unmodifiable map of all host service collections.
	 *
	 * @return an unmodifiable map of all host service collections.
	 */
	public Map<String, OSGiServiceCollection<T>> getServiceCollections() {
		return Collections.unmodifiableMap(services);
	}

	/**
	 * Returns the collection of available versions of a host service.
	 *
	 * @param pid PID of the host service.
	 * @return a collection of available versions of the host service.
	 */
	public OSGiServiceCollection<T> getServiceCollection(String pid) {
		return services.get(pid);
	}

}
