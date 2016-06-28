package ch.unifr.diva.dip.utils;

import ch.unifr.diva.dip.core.ApplicationHandler;
import ch.unifr.diva.dip.core.ui.UIStrategy;
import ch.unifr.diva.dip.eventbus.EventBus;
import ch.unifr.diva.dip.eventbus.events.StatusWorkerEvent;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for JavaFX {@code Task}s with optional status communication over an
 * event-bus.
 *
 * @param <T>
 */
public abstract class BackgroundTask<T> extends Task<T> {

	private static final Logger log = LoggerFactory.getLogger(BackgroundTask.class);
	private final UIStrategy uiStrategy;
	private final EventBus eventBus;

	public enum Result {

		/**
		 * The task was successful.
		 */
		SUCCEEDED,
		/**
		 * The task got cancelled.
		 */
		CANCELLED,
		/**
		 * The task failed.
		 */
		FAILED
	}

	public BackgroundTask() {
		this(null, null);
	}

	public BackgroundTask(EventBus eventBus) {
		this(null, eventBus);
	}

	public BackgroundTask(ApplicationHandler handler) {
		this(handler.uiStrategy, handler.eventBus);
	}

	public BackgroundTask(UIStrategy uiStrategy, EventBus eventBus) {
		this.uiStrategy = uiStrategy;
		this.eventBus = eventBus;
	}

	public Thread start() {
		final Thread thread = new Thread(this);
		if (eventBus != null) {
			eventBus.post(new StatusWorkerEvent(this));
		}
		thread.start();
		return thread;
	}

	/**
	 * Executes a runnable on the JavaFx application thread.
	 *
	 * @param runnable a runnable.
	 */
	protected void runLater(Runnable runnable) {
		Platform.runLater(runnable);
	}

	/**
	 * Hook method to clean up that get's called in {@code cancelled()} and
	 * {@code failed()} methods. Get's called before error handling in
	 * {@code failed()}. Also overwrite {@code succeeded()} in order to handle
	 * all possible outcomes (or just {@code finished()}).
	 */
	protected void cleanUp() {

	}

	/**
	 * Hook method that get's called either way; once done, cancelled, or
	 * failed. Get's called before {@code cleanUp()} and before error handling
	 * in {@code failed()}.
	 *
	 * @param state state of the BackgroundTask.
	 */
	protected void finished(Result state) {

	}

	@Override
	protected void succeeded() {
		finished(Result.SUCCEEDED);
	}

	@Override
	protected void cancelled() {
		finished(Result.CANCELLED);
		cleanUp();
	}

	@Override
	protected void failed() {
		finished(Result.FAILED);
		cleanUp();

		final Throwable throwable = this.exceptionProperty().get();
		if (throwable instanceof Exception) {
			// Exception (recoverable)
			final Exception ex = (Exception) throwable;
			log.error("background task throw an exception: ", ex);
		} else {
			// Error (unrecoverable)
			final Error error = (Error) throwable;
			log.error("background task produced an (unrecoverable) error: ", error);
		}

		if (uiStrategy != null) {
			uiStrategy.showError(throwable);
		}
	}
}
