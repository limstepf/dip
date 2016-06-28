
package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.services.Processor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

	default List<Service<Processor>> getCompatibleProcessors(Collection<String> inputTypes, Collection<String> outputTypes) {
		final List list = new ArrayList<>();
		for (Service<Processor> p : processors()) {
			final List<String> inputs = p.service.portTypes(p.service.inputs());
			final List<String> outputs = p.service.portTypes(p.service.outputs());

			boolean compatible = true;
			for (String in : inputTypes) {
				if (!inputs.contains(in)) {
					compatible = false;
					break;
				}
				inputs.remove(in);
			}
			if (!compatible) {
				continue;
			}
			for (String out : outputTypes) {
				if (!outputs.contains(out)) {
					compatible = false;
					break;
				}
				outputs.remove(out);
			}
			if (compatible) {
				list.add(p);
			}
		}
		return list;
	}

	default boolean isAvailable(String pid) {
		return (getService(pid) != null);
	}

	/**
	 * A service object.
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
