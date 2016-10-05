package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.osgi.OSGiServiceTracker.TrackerListener;
import ch.unifr.diva.dip.utils.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services monitor safe to be accessed from the JavaFX application thread.
 *
 * @param <T> class/interface of the service.
 */
public class OSGiServiceMonitor<T> implements ServiceMonitor<T> {

	private static final Logger log = LoggerFactory.getLogger(HostServiceMonitor.class);

	private final ObservableMap<String, OSGiServiceCollection<T>> services;
	private final ObservableList<OSGiServiceCollection<T>> collections;
	private final ServiceTrackerListener trackerListener;

	/**
	 * Creates a new OSGi service monitor.
	 *
	 * @param serviceTracker the OSGi service tracker of the service to monitor.
	 */
	public OSGiServiceMonitor(OSGiServiceTracker<T> serviceTracker) {
		this.services = FXCollections.observableHashMap();
		this.collections = FXCollections.observableArrayList();
		this.trackerListener = new ServiceTrackerListener();
		serviceTracker.addListener(trackerListener);
	}

	@Override
	public ObservableList<OSGiServiceCollection<T>> getServiceCollectionList() {
		return collections;
	}

	@Override
	public ObservableMap<String, OSGiServiceCollection<T>> getServiceCollectionMap() {
		return services;
	}

	/**
	 * A service tracker.
	 *
	 * @param <T> interface of the declarative service.
	 */
	private class ServiceTrackerListener implements TrackerListener<T> {

		@Override
		public void onAdded(OSGiServiceCollection<T> collection, OSGiService<T> service) {
			FxUtils.run(() -> {
				services.put(collection.pid(), collection);
				final int index = collections.indexOf(collection);
				if (index < 0) {
					collections.add(collection);
				} else {
					collections.set(index, collection);
				}
			});
		}

		@Override
		public void onModified(OSGiServiceCollection<T> collection, OSGiService<T> service) {
			FxUtils.run(() -> {
				final int index = collections.indexOf(collection);
				final String pid = collection.pid();
				if (!services.containsKey(pid) || index < 0) {
					return;
				}
				services.put(pid, collection);
				collections.set(index, collection);
			});
		}

		@Override
		public void onRemoved(OSGiServiceCollection<T> collection, OSGiService<T> service) {
			FxUtils.run(() -> {
				final int index = collections.indexOf(collection);
				final String pid = collection.pid();
				if (collection.numVersions() > 0 && services.containsKey(pid) && index >= 0) {
					services.put(pid, collection);
					collections.set(index, collection);
				} else {
					services.remove(pid);
					collections.remove(collection);
				}
			});
		}
	}

}
