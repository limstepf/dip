package ch.unifr.diva.dip.osgi;

import javafx.collections.ObservableList;

/**
 * Services monitor safe to be accessed from the JavaFX application thread.
 *
 * @param <T> interface of the declarative service.
 */
public interface ServiceMonitor<T> {

	/**
	 * Observable list of registered services.
	 *
	 * @return observable list of registered services.
	 */
	public ObservableList<Service<T>> services();

	/**
	 * Returns the requested service.
	 *
	 * @param pid PID of the service.
	 * @return the requested service, or null if not available.
	 */
	default Service<T> getService(String pid) {
		for (Service<T> s : services()) {
			if (s.pid.equals(pid)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Checks whether a service is available, or not.
	 *
	 * @param pid PID of the service.
	 * @return True if the service is available, False otherwise.
	 */
	default boolean isAvailable(String pid) {
		return (getService(pid) != null);
	}

	/**
	 * A service object.
	 *
	 * @param <T> type/interface of the service.
	 */
	public static class Service<T> {

		/**
		 * PID of the service.
		 */
		public final String pid;

		/**
		 * The service.
		 */
		public final T service;

		/**
		 * Creates a new service wrapper.
		 *
		 * @param pid PID of the service.
		 * @param service the service.
		 */
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
