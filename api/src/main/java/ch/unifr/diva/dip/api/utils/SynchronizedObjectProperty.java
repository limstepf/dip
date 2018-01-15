package ch.unifr.diva.dip.api.utils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A synchronized object property. A wrapped JavaFX object property with
 * {@code get()} and {@code set()} methods that can be safely called from any
 * thread.
 *
 * @param <T> class of the value of the property.
 */
public class SynchronizedObjectProperty<T> {

	private final ObjectProperty<T> property; // confined to the fx application thread!

	/**
	 * Creates a new synchronized object property.
	 */
	public SynchronizedObjectProperty() {
		this(null);
	}

	/**
	 * Creates a new synchronized object property.
	 *
	 * @param value the initial value.
	 */
	public SynchronizedObjectProperty(T value) {
		this.property = new SimpleObjectProperty<>(value);
	}

	/**
	 * Returns the property. Should be only accessed on the JavaFX application
	 * thread.
	 *
	 * @return the property.
	 */
	public ObjectProperty<T> getProperty() {
		return property;
	}

	/**
	 * Sets the value of the property. Safe to be called from any thread.
	 *
	 * @param value the new value of the property.
	 */
	public void set(T value) {
		FxUtils.runAndWait(() -> {
			property.set(value);
		});
	}

	/**
	 * Returns the value of the property. Safe to be called from any thread.
	 *
	 * @return the value of the property.
	 */
	public T get() {
		if (!Platform.isFxApplicationThread()) {
			final FutureTask<T> query = new FutureTask<>(() -> {
				return property.getValue();
			});
			Platform.runLater(query);
			try {
				return query.get();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException ex) {
				// shouldn't happen, and even if just return the property's
				// value no matter what thread...
			}
		}

		return property.getValue();
	}

}
