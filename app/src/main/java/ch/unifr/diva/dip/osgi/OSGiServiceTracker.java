package ch.unifr.diva.dip.osgi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Service Tracker. Not thread safe (e.g. to be accessed from the JavaFX
 * application thread).
 *
 * @param <T> class of the service interface of the tracked services.
 */
public class OSGiServiceTracker<T> {

	private static final Logger log = LoggerFactory.getLogger(OSGiServiceTracker.class);
	private final Class<T> service;
	private final BundleContext context;
	private final TrackerCustomizer customizer;
	private final ServiceTracker tracker;

	/**
	 * Creates a new OSGi service tracker.
	 *
	 * @param context the bundle context.
	 * @param service the class of the interface of the declarative service.
	 */
	public OSGiServiceTracker(BundleContext context, Class<T> service) {
		log.info("service tracker: {}", service);
		this.context = context;
		this.service = service;
		this.customizer = new TrackerCustomizer<>(context);
		this.tracker = new ServiceTracker(context, service.getName(), this.customizer);
	}

	/**
	 * Opens the service tracker.
	 */
	public final void open() {
		tracker.open();
	}

	/**
	 * Closes the service tracker.
	 */
	public final void close() {
		tracker.close();
	}

	/**
	 * Returns a map of currently tracked services.
	 *
	 * @return a map of PID -> tracked services.
	 */
	public Map<String, T> getServices() {
		return customizer.getServices();
	}

	/**
	 * Returns a service.
	 *
	 * @param pid PID of the requested service.
	 * @return the requested service, or null.
	 */
	public T getService(String pid) {
		return (T) customizer.getServices().get(pid);
	}

	/**
	 * Adds a tracker listener to the service tracker.
	 *
	 * @param listener the tracker listener.
	 */
	public void addListener(TrackerListener listener) {
		customizer.addListener(listener);
	}

	/**
	 * Removes a tracker listener from the service tracker.
	 *
	 * @param listener the tracker listener.
	 */
	public void removeListener(TrackerListener listener) {
		customizer.removeListener(listener);
	}

	/**
	 * Interface of a tracker listener.
	 *
	 * @param <T> class of the service interface of the tracked services.
	 */
	public interface TrackerListener<T> {

		/**
		 * Fires if a service has been added.
		 *
		 * @param pid PID of the service.
		 * @param service the service.
		 */
		public void onAdded(String pid, T service);

		/**
		 * Fires if a service has been modified.
		 *
		 * @param pid PID of the service.
		 * @param service the (modified) service.
		 */
		public void onModified(String pid, T service);

		/**
		 * Fies if a service has been removed.
		 *
		 * @param pid PID of the service.
		 * @param service the (removed) service.
		 */
		public void onRemoved(String pid, T service);

	}

	/**
	 * Service tracker customizer.
	 *
	 * @param <T> class of the service interface of the tracked services.
	 */
	private static class TrackerCustomizer<T> implements ServiceTrackerCustomizer {

		private final BundleContext context;
		private final Map<String, T> services;
		private final CopyOnWriteArrayList<TrackerListener> listeners;

		/**
		 * Creates a new service tracker customizer.
		 *
		 * @param context the bundle context.
		 */
		public TrackerCustomizer(BundleContext context) {
			this.context = context;
			services = new ConcurrentHashMap<>();
			listeners = new CopyOnWriteArrayList<>();
		}

		/**
		 * Returns a map of currently tracked services.
		 *
		 * @return a map of PID -> tracked services.
		 */
		public Map<String, T> getServices() {
			return services;
		}

		/**
		 * Adds a tracker listener.
		 *
		 * @param listener the tracker listener.
		 */
		public void addListener(TrackerListener listener) {
			listeners.add(listener);
		}

		/**
		 * Removes a tracker listener.
		 *
		 * @param listener the tracker listener.
		 */
		public void removeListener(TrackerListener listener) {
			listeners.remove(listener);
		}

		/**
		 * Returns the PID of a service (reference).
		 *
		 * @param reference the service reference.
		 * @return the PID of a service (reference).
		 */
		private String getPid(ServiceReference reference) {
			return (String) reference.getProperty("service.pid");
		}

		@Override
		public T addingService(ServiceReference reference) {
			final T service = (T) context.getService(reference);
			final String pid = getPid(reference);
			log.info("adding service: {}, {}", pid, reference);

			if (service != null && pid != null) {
				services.put(pid, service);

				for (TrackerListener listener : listeners) {
					listener.onAdded(pid, service);
				}
			}

			return service;
		}

		@Override
		public void modifiedService(ServiceReference reference, Object obj) {
			final T service = (T) obj;
			final String pid = getPid(reference);
			log.info("modified service: {}", pid);

			for (TrackerListener listener : listeners) {
				listener.onModified(pid, service);
			}
		}

		@Override
		public void removedService(ServiceReference reference, Object obj) {
			final T service = (T) obj;
			final String pid = getPid(reference);
			log.info("removed service: {}", pid);
			context.ungetService(reference);

			for (TrackerListener listener : listeners) {
				listener.onRemoved(pid, service);
			}
		}
	}

}
