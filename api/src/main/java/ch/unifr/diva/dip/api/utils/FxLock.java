package ch.unifr.diva.dip.api.utils;

import javafx.application.Platform;

/**
 * A simple lock to synchronize stuff on the FX application thread with any
 * other thread.
 *
 * <p>
 * Usage:
 * <pre>
 * <code>
 * // on some other thread
 * final FxLock lock = new FxLock();
 *
 * Platform.runLater(() -> {
 *    // stuff to do/update on the FX application thread
 *    // ...
 *    lock.notifyThread();
 * });
 *
 * lock.waitForFxApplicationThread();
 * </code>
 * </pre>
 */
public class FxLock {

	private volatile boolean notified;

	/**
	 * Notifies the other thread that we're done on the FX application thread.
	 */
	public void notifyThread() {
		synchronized (this) {
			notify();
			notified = true;
		}
	}

	/**
	 * Waits for the FX application thread to notify this one. Returns
	 * immediately if called from the FX application thread.
	 */
	public void waitForFxApplicationThread() {
		if (Platform.isFxApplicationThread()) {
			return;
		}
		synchronized (this) {
			if (notified) {
				return;
			}
			try {
				wait();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
