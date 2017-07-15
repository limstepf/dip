package ch.unifr.diva.dip.eventbus.events;

import javafx.concurrent.Worker;

/**
 * A status worker event.
 *
 * @param <T> class of the value/result of the worker.
 */
public class StatusWorkerEvent<T> {

	public final Worker<T> worker;

	/**
	 * Creates a new status worker event.
	 *
	 * @param worker the worker.
	 */
	public StatusWorkerEvent(Worker<T> worker) {
		this.worker = worker;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName()
				+ "{"
				+ "title=" + worker.getTitle()
				+ ", worker=" + worker
				+ "}";
	}

}
