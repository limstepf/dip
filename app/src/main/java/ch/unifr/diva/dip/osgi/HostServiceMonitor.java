package ch.unifr.diva.dip.osgi;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Host processor services monitor safe to be accessed from the JavaFX
 * application thread.
 *
 * @param <T> class/interface of the service.
 */
public class HostServiceMonitor<T> implements ServiceMonitor<T> {

	private final ObservableMap<String, OSGiServiceCollection<T>> processors;
	private final ObservableList<OSGiServiceCollection<T>> collections;

	/**
	 * Creates a new host processor service monitor.
	 *
	 * @param tracker the host service tracker.
	 */
	public HostServiceMonitor(HostServiceTracker<T> tracker) {
		this.processors = FXCollections.observableHashMap();
		this.collections = FXCollections.observableArrayList();

		for (String pid : tracker.getServiceCollections().keySet()) {
			final OSGiServiceCollection<T> collection = tracker.getServiceCollection(pid);
			processors.put(pid, collection);
			collections.add(collection);
		}
	}

	@Override
	public ObservableList<OSGiServiceCollection<T>> getServiceCollectionList() {
		return collections;
	}

	@Override
	public ObservableMap<String, OSGiServiceCollection<T>> getServiceCollectionMap() {
		return processors;
	}

}
