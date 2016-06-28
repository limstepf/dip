package ch.unifr.diva.dip.eventbus;

/**
 * EventBus interface.
 */
public interface EventBus {
	/**
	 * Register/subscribe an object to the event-bus.
	 * @param object
	 */
	public void register(Object object);

	/**
	 * Unregister/unsubscribe an object from the event-bus.
	 * @param object
	 */
	public void unregister(Object object);

	/**
	 * Post an event to the event-bus.
	 * @param event
	 */
	public void post(Object event);
}
