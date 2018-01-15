package ch.unifr.diva.dip.utils;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A fancy "dirty-bit". A ModifiedProperty is a fancy "dirty-bit" in form of a
 * BooleanProperty extended:
 *
 * <ul>
 * <li>to observe the (modifiable) parent object its own properties, and</li>
 * <li>able to manage - that is listen to (modifications) and clear (upon
 * saving) - child objects that are modifiable (by implementing the Modifiable
 * interface).</li>
 * </ul>
 */
public class ModifiedProperty {

	private final BooleanProperty modifiedProperty;
	private final List<Modifiable> managedModifiables;
	private final InvalidationListener invalidationListener;
	private final ChangeListener<Boolean> modifiedListener;

	/**
	 * Default constructor creating a modified property initially set to {@code false}.
	 */
	public ModifiedProperty() {
		this.modifiedProperty = new SimpleBooleanProperty(false);
		this.managedModifiables = new ArrayList<>();
		this.invalidationListener = (Observable observable) -> {
			modifiedProperty.set(true);
		};
		this.modifiedListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
			if (newValue) {
				this.set(true);
			}
		};
	}

	/**
	 * Add an observable to listen to for modifications. An observed property
	 * that got invalidated will set this property to modified.
	 *
	 * @param observable an observable to listen to.
	 */
	public void addObservedProperty(Observable observable) {
		observable.addListener(invalidationListener);
	}

	/**
	 * Stop listening to an obserrvable for modifications.
	 *
	 * @param observable the observable to no longer listen to.
	 */
	public void removeObservedProperty(Observable observable) {
		observable.removeListener(invalidationListener);
	}

	/**
	 * Add a modifiable (object) to be managed by this property. Managed
	 * modifiables will be cleared/set to unmodified if this property is
	 * cleared/set to unmodified. The other way around: this property will be
	 * set to modified if the modifiable (object) has been modified.
	 *
	 * @param modifiable a modifiable (object) to be managed by this property.
	 */
	public void addManagedProperty(Modifiable modifiable) {
		modifiable.modifiedProperty().addListener(modifiedListener);
		managedModifiables.add(modifiable);
	}

	/**
	 * Stop to manage a modifiable (object).
	 *
	 * @param modifiable the modifiable (object) to be managed no longer.
	 */
	public void removeManagedProperty(Modifiable modifiable) {
		if (modifiable == null) {
			return;
		}
		modifiable.modifiedProperty().removeListener(modifiedListener);
		managedModifiables.remove(modifiable);
	}

	/**
	 * Changes the modififaction state of this property. This method replaces
	 * the usual {@code set()} method (not exposed here) on your ordinary
	 * properties.
	 *
	 * <p>
	 * If set to unmodified all managed properties will be set to unmodified as
	 * well.
	 *
	 * @param modified {@code true} to set this property to modified,
	 * {@code false} otherwise.
	 */
	public final void set(boolean modified) {
		if (!modified) {
			for (Modifiable m : managedModifiables) {
				m.modifiedProperty().set(false);
			}
		}
		modifiedProperty.set(modified);
	}

	/**
	 * Checks whether the property is {@code true} (or the parent object is
	 * marked as modified respectively) or not.
	 *
	 * @return {@code true} if the parent object is markes as modified,
	 * {@code false} otherwise.
	 */
	public boolean get() {
		return modifiedProperty.get();
	}

	/**
	 * Returns an observable boolean value to bind to.
	 *
	 * @return an observable boolean value.
	 */
	public ObservableValue<Boolean> getObservableValue() {
		return (ObservableValue<Boolean>) modifiedProperty;
	}

	/* decorated Property/ObservableValue/Observable methods */
	/**
	 * Adds an {@link InvalidationListener} which will be notified whenever the
	 * {@code Observable} becomes invalid.
	 *
	 * @param listener the listener to register.
	 */
	public void addListener(InvalidationListener listener) {
		modifiedProperty.addListener(listener);
	}

	/**
	 * Removes the given listener from the list of listeners, that are notified
	 * whenever the value of the {@code Observable} becomes invalid.
	 *
	 * @param listener the listener to remove.
	 */
	public void removeListener(InvalidationListener listener) {
		modifiedProperty.removeListener(listener);
	}

	/**
	 * Adds a {@link ChangeListener} which will be notified whenever the value
	 * of the {@code ObservableValue} changes.
	 *
	 * @param listener the listener to register.
	 */
	public void addListener(ChangeListener<? super Boolean> listener) {
		modifiedProperty.addListener(listener);
	}

	/**
	 * Removes the given listener from the list of listeners, that are notified
	 * whenever the value of the {@code ObservableValue} changes.
	 *
	 * @param listener the listener to remove.
	 */
	public void removeListener(ChangeListener<? super Boolean> listener) {
		modifiedProperty.removeListener(listener);
	}
}
