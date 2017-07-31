package ch.unifr.diva.dip.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.osgi.framework.Version;

/**
 * Services monitor safe to be accessed from the JavaFX application thread.
 *
 * @param <T> interface of the declarative service.
 */
public interface ServiceMonitor<T> {

	/**
	 * Returns an observable list of all tracked service collections.
	 *
	 * @return an observable list of service collections.
	 */
	public ObservableList<OSGiServiceCollection<T>> getServiceCollectionList();

	/**
	 * Returns a sorted list of all tracked service collections.
	 *
	 * @return a sorted list of service collections.
	 */
	default List<OSGiServiceCollection<T>> getSortedServiceCollectionList() {
		final List<OSGiServiceCollection<T>> list = new ArrayList<>(getServiceCollectionList());
		Collections.sort(list, (OSGiServiceCollection<T> t, OSGiServiceCollection<T> t1) -> {
			return t.pid().compareTo(t1.pid());
		});
		return list;
	}

	/**
	 * Returns an observable map of all tracked service collections.
	 *
	 * @return an observable map of service collections, indexed by their PID.
	 */
	public ObservableMap<String, OSGiServiceCollection<T>> getServiceCollectionMap();

	/**
	 * Returns a service collection.
	 *
	 * @param pid the PID of the service collection.
	 * @return the service collection, or {@code null} if not available.
	 */
	default OSGiServiceCollection<T> getServiceCollection(String pid) {
		return getServiceCollectionMap().get(pid);
	}

	/**
	 * Returns the latest version of a service.
	 *
	 * @param pid the PID of the service.
	 * @return the latest service, or {@code null} if not available.
	 */
	default OSGiService<T> getService(String pid) {
		final OSGiServiceCollection<T> collection = getServiceCollection(pid);
		if (collection == null) {
			return null;
		}
		return collection.getService();
	}

	/**
	 * Checks whether a service (any version) is available.
	 *
	 * @param pid PID of the service.
	 * @return {@code true} if the service is available, {@code false}
	 * otherwise.
	 */
	default boolean isAvailable(String pid) {
		return getService(pid) != null;
	}

	/**
	 * Returns a certain version of a service.
	 *
	 * @param pid the PID of the service.
	 * @param major the major version.
	 * @return the latest service with given major version, or {@code null} if
	 * not available.
	 */
	default OSGiService<T> getService(String pid, int major) {
		final OSGiServiceCollection<T> collection = getServiceCollection(pid);
		if (collection == null) {
			return null;
		}
		return collection.getService(major);
	}

	/**
	 * Checks whether a certain version of a service is available.
	 *
	 * @param pid the PID of the service.
	 * @param major the major version.
	 * @return {@code true} if the service is available, {@code false}
	 * otherwise.
	 */
	default boolean isAvailable(String pid, int major) {
		return getService(pid, major) != null;
	}

	/**
	 * Returns a certain version of a service.
	 *
	 * @param pid the PID of the service.
	 * @param major the major version.
	 * @param minor the minor version.
	 * @return the latest service with given major and minor version, or
	 * {@code null} if not available.
	 */
	default OSGiService<T> getService(String pid, int major, int minor) {
		final OSGiServiceCollection<T> collection = getServiceCollection(pid);
		if (collection == null) {
			return null;
		}
		return collection.getService(major, minor);
	}

	/**
	 * Checks whether a certain version of a service is available.
	 *
	 * @param pid the PID of the service.
	 * @param major the major version.
	 * @param minor the minor version.
	 * @return {@code true} if the service is available, {@code false}
	 * otherwise.
	 */
	default boolean isAvailable(String pid, int major, int minor) {
		return getService(pid, major, minor) != null;
	}

	/**
	 * Returns a certain version of a service.
	 *
	 * @param pid the PID of the service.
	 * @param major the major version.
	 * @param minor the minor version.
	 * @param micro the micro version.
	 * @return the requested service, or {@code null} if not available.
	 */
	default OSGiService<T> getService(String pid, int major, int minor, int micro) {
		final OSGiServiceCollection<T> collection = getServiceCollection(pid);
		if (collection == null) {
			return null;
		}
		return collection.getService(major, minor, micro);
	}

	/**
	 * Returns a specific version of a service.
	 *
	 * @param pid the PID of the service.
	 * @param version the semantic version of the service.
	 * @return the requested service, or {@code null} if not available.
	 */
	default OSGiService<T> getService(String pid, Version version) {
		final OSGiServiceCollection<T> collection = getServiceCollection(pid);
		if (collection == null) {
			return null;
		}
		return collection.getService(version);
	}

	/**
	 * Checks whether a certain version of a service is available.
	 *
	 * @param pid the PID of the service.
	 * @param major the major version.
	 * @param minor the minor version.
	 * @param micro the micro version.
	 * @return {@code true} if the service is available, {@code false}
	 * otherwise.
	 */
	default boolean isAvailable(String pid, int major, int minor, int micro) {
		return getService(pid, major, minor, micro) != null;
	}

	/**
	 * Checks whether a specific version of a service is available.
	 *
	 * @param pid the PID of the service.
	 * @param version the semantic version of the service.
	 * @return {@code true} if the service is available, {@code false}
	 * otherwise.
	 */
	default boolean isAvailable(String pid, String version) {
		return isAvailable(pid, new Version(version));
	}

	/**
	 * Checks whether a specific version of a service is available.
	 *
	 * @param pid the PID of the service.
	 * @param version the semantic version of the service.
	 * @return {@code true} if the service is available, {@code false}
	 * otherwise.
	 */
	default boolean isAvailable(String pid, Version version) {
		return getService(pid, version) != null;
	}

	/**
	 * Waits for bundles to be registered. This method keeps waiting for as long
	 * as new bundles are being registered, or until it times out. The number of
	 * registered bundles is repolled each {@code delay} milliseconds, and keeps
	 * repolling in case the number keeps increasing. Once that's not the case,
	 * we repoll again {@code repeat} times (just to be sure...), meaning that
	 * the minimum waiting time is {@code repeat * delay} even if no bundles are
	 * being registered.
	 *
	 * @param repeat number of times to repoll in case the number of registered
	 * bundles didn't increase.
	 * @param delay polling delay in milliseconds.
	 * @param timeout timeout in milliseconds.
	 * @return {@code true} if all bundles have been loaded without timing out,
	 * {@code false} otherwise.
	 * @throws java.lang.InterruptedException
	 */
	default boolean waitForBundles(int repeat, long delay, long timeout) throws InterruptedException {
		return true;
	}

}
