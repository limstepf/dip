package ch.unifr.diva.dip.utils;

import ch.unifr.diva.dip.api.utils.FxUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A synchronized object property. Can be set from any thread, and exposes a
 * read only property confined to the JavaFX application thread.
 *
 * @param <T> class of the value of the property.
 */
public class SynchronizedObjectProperty<T> {

	private final ObjectProperty<T> property; // confined to the fx application thread!
	private volatile T value;

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
		this.value = value;
		this.property = new SimpleObjectProperty<>(value);
	}

	/**
	 * Returns the read-only property. Should be only accessed on the JavaFX
	 * application thread.
	 *
	 * @return the read-only property.
	 */
	public ReadOnlyObjectProperty<T> getReadOnlyProperty() {
		return property;
	}

	/**
	 * Sets the value of the property. Save to be called from any thread.
	 *
	 * @param value the new value of the property.
	 */
	public synchronized void set(T value) {
		this.value = value;
		FxUtils.run(() -> property.set(value));
	}

	/**
	 * Returns the value of the property. Save to be called from any thread.
	 *
	 * @return the value of the property.
	 */
	public synchronized T get() {
		return value;
	}

}
