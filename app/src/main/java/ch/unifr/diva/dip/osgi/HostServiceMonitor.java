package ch.unifr.diva.dip.osgi;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.ReflectionUtils;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Host processor services monitor safe to be accessed from the JavaFX
 * application thread.
 */
public class HostServiceMonitor implements ServiceMonitor {

	private static final Logger log = LoggerFactory.getLogger(HostServiceMonitor.class);
	private static final String DIP_HOST_SERVICES_PACKAGE = "ch.unifr.diva.dip.core.services";
	private final ObservableList<Service<Processor>> processors;

	/**
	 * Creates a new host processor service monitor.
	 */
	public HostServiceMonitor() {
		this.processors = FXCollections.observableArrayList();
		String pid = "-1";
		try {
			List<String> classes = ReflectionUtils.findClasses(DIP_HOST_SERVICES_PACKAGE);
			for (String cn : classes) {
				pid = cn;
				Class<?> clazz = ReflectionUtils.getClass(cn);
				Processor service = (Processor) clazz.newInstance();
				processors.add(new Service(pid, service));
			}
		} catch (IOException ex) {
			log.error("can't find package: {}", DIP_HOST_SERVICES_PACKAGE);
		} catch (InstantiationException | IllegalAccessException ex) {
			log.error("invalid host service: {}", pid);
		}
	}

	@Override
	public ObservableList<Service<Processor>> services() {
		return this.processors;
	}

}
