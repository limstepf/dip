package ch.unifr.diva.dip.osgi;

import java.util.Arrays;
import java.util.List;
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
 * OSGi Service Tracker.
 *
 * @param <T> Class of the service interface of the tracked services.
 */
public class OSGiServiceTracker<T> {

	private static final Logger log = LoggerFactory.getLogger(OSGiServiceTracker.class);
	private final Class<T> service;
	private final BundleContext context;
	private final TrackerCustomizer customizer;
	private final ServiceTracker tracker;

	public OSGiServiceTracker(BundleContext context, Class<T> service) {
		log.info("service tracker: {}", service);
		this.context = context;
		this.service = service;
		this.customizer = new TrackerCustomizer<>(context);
		this.tracker = new ServiceTracker(context, service.getName(), this.customizer);
	}

	public final void open() {
		tracker.open();
	}

	public final void close() {
		tracker.close();
	}

	public Map<String, T> getServices() {
		return customizer.getServices();
	}

	public T getService(String pid) {
		return (T) customizer.getServices().get(pid);
	}

	public void addListener(TrackerListener listener) {
		customizer.addListener(listener);
	}

	public void removeListener(TrackerListener listener) {
		customizer.removeListener(listener);
	}

	public interface TrackerListener<T> {

		public void onAdded(String pid, T service);

		public void onModified(String pid, T service);

		public void onRemoved(String pid, T service);
	}

	private static class TrackerCustomizer<T> implements ServiceTrackerCustomizer {

		private final BundleContext context;
		private final Map<String, T> services;
		private final CopyOnWriteArrayList<TrackerListener> listeners;

		public TrackerCustomizer(BundleContext context) {
			this.context = context;
			services = new ConcurrentHashMap<>();
			listeners = new CopyOnWriteArrayList<>();
		}

		public Map<String, T> getServices() {
			return services;
		}

		public void addListener(TrackerListener listener) {
			listeners.add(listener);
		}

		public void removeListener(TrackerListener listener) {
			listeners.remove(listener);
		}

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
