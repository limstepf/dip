package ch.unifr.diva.dip.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi Service Tracker. Thread-safe.
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
		this.context = context;
		this.service = service;
		this.customizer = new TrackerCustomizer<>(context);
		this.tracker = new ServiceTracker(context, service.getName(), this.customizer);

		log.debug("starting service tracker: {}", service);
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
	 * Returns a copy of a list of all tracked service collections.
	 *
	 * @return a list of all service collections.
	 */
	public List<ServiceCollection<T>> getServiceCollectionList() {
		return customizer.getServiceCollectionList();
	}

	/**
	 * Returns a copy of the map of all tracked service collections.
	 *
	 * @return a map of all service collections, indexed by their PID.
	 */
	public Map<String, ServiceCollection<T>> getServiceCollectionMap() {
		return customizer.getServiceCollectionMap();
	}

	/**
	 * Returns the collection of available versions of a service.
	 *
	 * @param pid PID of the service.
	 * @return a collection of available versions of the service.
	 */
	public ServiceCollection<T> getServiceCollection(String pid) {
		return customizer.getServiceCollection(pid);
	}

	/**
	 * Wait for at least one service to be tracked by this ServiceTracker. This
	 * method will also return when this ServiceTracker is closed
	 *
	 * @param timeout The time interval in milliseconds to wait. If zero, the
	 * method will wait indefinitely.
	 * @throws InterruptedException If another thread has interrupted the
	 * current thread.
	 */
	public void waitForService(long timeout) throws InterruptedException {
		this.tracker.waitForService(timeout);
	}

	/**
	 * Waits for bundles to be registered. This method keeps waiting for as long
	 * as new bundles are being registered, or until it times out. The number of
	 * registered bundles is repolled each {@code delay} milliseconds, and keeps
	 * repolling in case the number keeps increasing. Once that's not the case,
	 * we repoll again {@code repeat} times (just to be sure...), meaning that
	 * the minimum waiting time is {@code (repeat + 1) * delay} even if no
	 * bundles are being registered.
	 *
	 * @param repeat number of times to repoll in case the number of registered
	 * bundles didn't increase.
	 * @param delay polling delay in milliseconds.
	 * @param timeout timeout in milliseconds.
	 * @return {@code true} if all bundles have been loaded without timing out,
	 * {@code false} otherwise.
	 * @throws java.lang.InterruptedException
	 */
	public boolean waitForBundles(int repeat, long delay, long timeout) throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		new Thread(() -> {
			int r = repeat; // # we repoll, in case tracking count didn't increase
			int n = 0;
			int m = tracker.getTrackingCount();
			while (n < m || m < 1) {
				try {
					Thread.sleep(delay);
					n = m;
					m = tracker.getTrackingCount();
					if (n < m && r > 0) {
						r--;
						n--;
					} else {
						r = repeat;
					}
				} catch (InterruptedException ex) {

				}
			}
			latch.countDown();
		}).start();

		return latch.await(timeout, TimeUnit.MILLISECONDS);
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
		 * @param collection the service collection of the OSGi service.
		 * @param service the OSGi service.
		 */
		public void onAdded(OSGiServiceCollection<T> collection, OSGiService<T> service);

		/**
		 * Fires if a service has been modified.
		 *
		 * @param collection the service collection of the OSGi service.
		 * @param service the OSGi service.
		 */
		public void onModified(OSGiServiceCollection<T> collection, OSGiService<T> service);

		/**
		 * Fires if a service has been removed.
		 *
		 * @param collection the service collection of the OSGi service.
		 * @param service the OSGi service.
		 */
		public void onRemoved(OSGiServiceCollection<T> collection, OSGiService<T> service);

	}

	/**
	 * Service tracker customizer. Thread-safe.
	 *
	 * @param <T> class of the service interface of the tracked services.
	 */
	private static class TrackerCustomizer<T> implements ServiceTrackerCustomizer {

		private final BundleContext context;
		private final Map<String, OSGiServiceCollection<T>> services;
		private final CopyOnWriteArrayList<TrackerListener> listeners;

		/**
		 * Creates a new service tracker customizer.
		 *
		 * @param context the bundle context.
		 */
		public TrackerCustomizer(BundleContext context) {
			this.context = context;
			this.services = new HashMap<>();
			this.listeners = new CopyOnWriteArrayList<>();
		}

		/**
		 * Returns a copy of a list of all tracked service collections.
		 *
		 * @return a list of all service collections.
		 */
		public synchronized List<OSGiServiceCollection<T>> getServiceCollectionList() {
			return new ArrayList<>(services.values());
		}

		/**
		 * Returns a copy of the map of all tracked service collections.
		 *
		 * @return a map of all service collections, indexed by their PID.
		 */
		public synchronized Map<String, OSGiServiceCollection<T>> getServiceCollectionMap() {
			return new HashMap<>(services);
		}

		/**
		 * Returns the collection of available versions of a service.
		 *
		 * @param pid PID of the service.
		 * @return a collection of available versions of the service.
		 */
		public synchronized OSGiServiceCollection<T> getServiceCollection(String pid) {
			return services.get(pid);
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

		@Override
		public T addingService(ServiceReference reference) {
			final OSGiService<T> service;
			try {
				service = new OSGiService(context, reference);
			} catch (OSGiInvalidServiceException ex) {
				log.warn("can't add service: {}", ex.getMessage());
				return null; // ignore/do not add modified service
			}
			log.debug("add service: {}", service);

			final OSGiServiceCollection<T> collection;
			synchronized (this) {
				if (!services.containsKey(service.pid)) {
					collection = new OSGiServiceCollection(service.pid);
					services.put(service.pid, collection);
				} else {
					collection = services.get(service.pid);
				}
				collection.add(service);
			}

			for (TrackerListener listener : listeners) {
				listener.onAdded(collection, service);
			}

			return service.serviceObject;
		}

		/*
		 * modifiedService is called when a service being tracked by the
		 * ServiceTracker has had its properties modified (so probably never...).
		 * Other than that, services are usually just added, removed, and
		 * re-added again, ... Anyways.
		 */
		@Override
		public void modifiedService(ServiceReference reference, Object obj) {
			final OSGiService<T> service;
			try {
				service = new OSGiService(context, reference);
			} catch (OSGiInvalidServiceException ex) {
				log.warn("can't update service: {}", ex.getMessage());
				return; // ignore/do not update modified service
			}
			log.debug("update service: {}", service);

			final OSGiServiceCollection<T> collection;
			synchronized (this) {
				collection = services.get(service.pid);
				if (collection == null) {
					return;
				}

				collection.update(service);
			}

			for (TrackerListener listener : listeners) {
				listener.onModified(collection, service);
			}
		}

		@Override
		public void removedService(ServiceReference reference, Object obj) {
			final OSGiService<T> service;
			try {
				service = new OSGiService(context, reference);
			} catch (OSGiInvalidServiceException ex) {
				log.warn("can't remove service: {}", ex.getMessage());
				return; // ignore/do not remove modified service
			}
			log.debug("remove service: {}", service);

			final OSGiServiceCollection<T> collection;
			synchronized (this) {
				collection = services.get(service.pid);
				if (collection == null) {
					return;
				}

				context.ungetService(reference);
				collection.remove(service);
			}

			for (TrackerListener listener : listeners) {
				listener.onRemoved(collection, service);
			}
		}

	}

}
