package ch.unifr.diva.dip.eventbus;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Google Guava's EventBus.
 */
public class EventBusGuava implements EventBus {
	private static final Logger log = LoggerFactory.getLogger(EventBusGuava.class);
	private final com.google.common.eventbus.EventBus eventBus;
	private final DeadEventListener deadEventListener;

	public EventBusGuava() {
		eventBus = new com.google.common.eventbus.EventBus(new ExceptionHandler());
		deadEventListener = new DeadEventListener();
		eventBus.register(deadEventListener);
	}

	@Override
	public void register(Object object) {
		eventBus.register(object);
	}

	@Override
	public void unregister(Object object) {
		eventBus.unregister(object);
	}

	@Override
	public void post(Object event) {
		log.info("posting event: {}", event);
		eventBus.post(event);
	}

	/**
	 * Dead event listener.
	 * Catches all dead events (republished events that have not been catched
	 * by some subscriber).
	 */
	private static class DeadEventListener {
		@Subscribe
		public void onDeadEvent(DeadEvent event) {
			log.warn("catched a dead event: {}", event.getEvent());
		}
	}

	/**
	 * Event Subscriber Exception Handler.
	 * Handlers should not, in general, throw.
	 */
	private static class ExceptionHandler implements SubscriberExceptionHandler {
		@Override
		public void handleException(Throwable exception, SubscriberExceptionContext context) {
			log.error("subscriber exception (event: {}, subscriber: {})", context.getEvent(), context.getSubscriber(), exception);
		}
	}
}
