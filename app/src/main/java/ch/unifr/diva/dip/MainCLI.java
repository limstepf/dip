package ch.unifr.diva.dip;

import ch.unifr.diva.dip.core.ApplicationContext;
import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategy;
import ch.unifr.diva.dip.core.ui.UIStrategyCLI;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.EventBusGuava;
import ch.unifr.diva.dip.eventbus.events.StatusMessageEvent;
import ch.unifr.diva.dip.eventbus.events.StatusWorkerEvent;
import ch.unifr.diva.dip.api.utils.FxUtils;
import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command-line interface (CLI) of the application.
 */
public class MainCLI {
	private static final Logger log = LoggerFactory.getLogger(MainCLI.class);
	private final EventBus eventBus;
	private final UIStrategy uiStrategy;
	private final ApplicationHandler handler;
	private final Map<String, List<String>> params;

	public MainCLI(ApplicationContext context, Map<String, List<String>> params) {
		// verify that we have a valid context - or shut down.
		if (!context.getErrors().isEmpty()) {
			printErrors(context.getErrors());
			this.uiStrategy = null;
			this.eventBus = null;
			this.handler = null;
			this.params = null;
			return; // bye
		}

		this.uiStrategy = new UIStrategyCLI();
		this.eventBus = new EventBusGuava();
		this.handler = new ApplicationHandler(context, uiStrategy, eventBus);
		this.params = params;

		StatusListener status = new StatusListener();
		eventBus.register(status);

		// manually initialize the JavaFX toolkit
		FxUtils.initToolkit();

		// ...
	}

	private void printErrors(List<ApplicationContext.ContextError> errors) {
		for (ApplicationContext.ContextError error : errors) {
			log.error(error.message, error.exception);
		}
	}

	private class StatusListener {
		@Subscribe
		public void statusEvent(StatusWorkerEvent event) {
			// TODO: log worker messages
		}

		@Subscribe
		public void statusEvent(StatusMessageEvent event) {
			log.info(event.message);
		}
	}
}
