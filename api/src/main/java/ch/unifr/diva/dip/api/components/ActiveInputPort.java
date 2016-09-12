package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * An active input port. Active input ports have an additional
 * {@code valueChanged} property that signals when the value on that port (or on
 * the connected output port respectively) has changed.
 *
 * <p>
 * Processors that implement the {@code Previewable} interface and are fed by
 * processors that immediately change/update their outputs upon changing their
 * parameters (e.g. the matrix editor) should use {@code ActiveInputPorts}
 * instead of regular ones, s.t. the preview of the processor immediately
 * updates if the (also opened) producer processor changes.
 *
 * @param <T> type of the port.
 */
public class ActiveInputPort<T> extends InputPort<T> {

	/**
	 * Creates a new, active input port.
	 *
	 * @param dataType data type of the port.
	 * @param required flag if the port is absolutely required to work.
	 */
	public ActiveInputPort(DataType dataType, boolean required) {
		super(dataType, required);
	}

	@Override
	protected void setReady(boolean ready) {
		super.setReady(ready);
		onValueChanged();
	}

	// we do this lazily, since this is only usefull for processors in a runnable
	// pipeline
	private BooleanProperty valueChangedProperty;

	/**
	 * Returns the value changed property. This property is lazily initialized,
	 * and fires each time the producer for this port changed the signal/value.
	 * That is: when the connected output port changed its signal/value.
	 *
	 * <p>
	 * Most of the time it's good enough to listen to the port state property.
	 * But that one does not fire on updated signals/value, where the port state
	 * of this port remains in a {@code READY} state. This property get's
	 * invalidated all the time. The value of the property itself, however, is
	 * just a boolean that gets flipped.
	 *
	 * @return the value changed property.
	 */
	public ReadOnlyBooleanProperty valueChangedProperty() {
		if (valueChangedProperty == null) {
			valueChangedProperty = new SimpleBooleanProperty();
		}
		return valueChangedProperty;
	}

	// flips the value changed property's boolean; only if it got instantiated
	protected void onValueChanged() {
		if (valueChangedProperty == null) {
			return;
		}
		valueChangedProperty.set(!valueChangedProperty.get());
	}

}
