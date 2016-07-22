package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.osgi.OSGiServiceTracker.TrackerListener;
import ch.unifr.diva.dip.utils.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services monitor safe to be accessed from the JavaFX application thread.
 *
 * @param <T> interface of the declarative service.
 */
public class OSGiServiceMonitor<T> implements ServiceMonitor<T> {

	private static final Logger log = LoggerFactory.getLogger(HostServiceMonitor.class);
	private final ServiceTrackerListener trackerListener;
	private final ObservableList<Service<T>> services;

	/**
	 * Creates a new OSGi service monitor.
	 *
	 * @param serviceTracker the OSGi service tracker of the service to monitor.
	 */
	public OSGiServiceMonitor(OSGiServiceTracker<T> serviceTracker) {
		services = FXCollections.observableArrayList();
		trackerListener = new ServiceTrackerListener();
		serviceTracker.addListener(trackerListener);
	}

	@Override
	public ObservableList<Service<T>> services() {
		return services;
	}

	/**
	 * A service tracker.
	 *
	 * @param <T> interface of the declarative service.
	 */
	private class ServiceTrackerListener<T> implements TrackerListener<T> {

		@Override
		public void onAdded(String pid, T service) {
			FxUtils.run(() -> {
				services.add(new Service(pid, service));
			});
		}

		@Override
		public void onModified(String pid, T service) {
			FxUtils.run(() -> {
				int index = services.indexOf(pid);
				if (index < 0) {
					log.warn("failed to update service {}: invalid index.", pid);
					return;
				}
				services.set(index, new Service(pid, service));
			});
		}

		@Override
		public void onRemoved(String pid, T service) {
			FxUtils.run(() -> {
				services.remove(new Service(pid, service));
			});
		}
	}

}
