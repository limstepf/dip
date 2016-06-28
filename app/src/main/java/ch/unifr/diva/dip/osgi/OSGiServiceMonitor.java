
package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.osgi.OSGiServiceTracker.TrackerListener;
import ch.unifr.diva.dip.utils.FxUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services monitor safe to be accessed from the JavaFX application thread.
 */
public class OSGiServiceMonitor implements ServiceMonitor {

	private static final Logger log = LoggerFactory.getLogger(HostServiceMonitor.class);
	private final ProcessorTracker processorTracker;
	private final ObservableList<Service<Processor>> processors;

	public OSGiServiceMonitor(OSGiServiceTracker<Processor> serviceTracker) {
		processors = FXCollections.observableArrayList();
		processorTracker = new ProcessorTracker();
		serviceTracker.addListener(processorTracker);
	}

	@Override
	public ObservableList<Service<Processor>> processors() {
		return processors;
	}

	private class ProcessorTracker implements TrackerListener<Processor> {

		@Override
		public void onAdded(String pid, Processor service) {
			FxUtils.run(() -> {
				processors.add(new Service(pid, service));
			});
		}

		@Override
		public void onModified(String pid, Processor service) {
			FxUtils.run(() -> {
				int index = processors.indexOf(pid);
				if (index < 0) {
					log.warn("failed to update service {}: invalid index.", pid);
					return;
				}
				processors.set(index, new Service(pid, service));
			});
		}

		@Override
		public void onRemoved(String pid, Processor service) {
			FxUtils.run(() -> {
				processors.remove(new Service(pid, service));
			});
		}
	}

}
