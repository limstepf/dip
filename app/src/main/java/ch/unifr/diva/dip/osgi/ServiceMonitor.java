package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.services.Processor;
import javafx.collections.ObservableList;

/**
 * Services monitor safe to be accessed from the JavaFX application thread.
 */
public interface ServiceMonitor {

	public ObservableList<Service<Processor>> processors();

	default Service<Processor> getService(String pid) {
		for (Service<Processor> s : processors()) {
			if (s.pid.equals(pid)) {
				return s;
			}
		}
		return null;
	}

	default boolean isAvailable(String pid) {
		return (getService(pid) != null);
	}

	/**
	 * A service object.
	 *
	 * @param <T> type/interface of the service.
	 */
	public static class Service<T> {

		public final String pid;
		public final T service;

		public Service(String pid, T service) {
			this.pid = pid;
			this.service = service;
		}

		@Override
		public int hashCode() {
			return pid.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Service other = (Service) obj;
			return this.pid.equals(other.pid);
		}
	}
}
