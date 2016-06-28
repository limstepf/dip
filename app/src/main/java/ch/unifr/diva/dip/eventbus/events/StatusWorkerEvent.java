package ch.unifr.diva.dip.eventbus.events;

import javafx.concurrent.Worker;

/**
 * StatusWorkerEvent.
 */
public class StatusWorkerEvent {

	public final Worker worker;

	public StatusWorkerEvent(Worker worker) {
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
