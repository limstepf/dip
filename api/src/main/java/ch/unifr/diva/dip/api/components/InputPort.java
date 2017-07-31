package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Input port. Input ports provide objects to DIP processors.
 *
 * @param <T> type of the value on the port (i.e. not the DataType).
 */
public class InputPort<T> extends AbstractPort<T> {

	private OutputPort<T> output;

	/**
	 * Creates a new input port with a default label.
	 *
	 * @param dataType data type of the port.
	 * @param required flag if the port is absolutely required to work.
	 */
	public InputPort(DataType<T> dataType, boolean required) {
		this("", dataType, required);
	}

	/**
	 * Creates a new input port.
	 *
	 * @param label a custom port label (short and descriptive).
	 * @param dataType data type of the port.
	 * @param required flag if the port is absolutely required to work.
	 */
	public InputPort(String label, DataType<T> dataType, boolean required) {
		super(label, dataType, required);
		setPortState(State.UNCONNECTED);
	}

	@Override
	public boolean isConnected() {
		return !getPortState().equals(State.UNCONNECTED);
	}

	@Override
	public Set<OutputPort<T>> connections() {
		final Set<OutputPort<T>> outputs = new HashSet<>();
		if (isConnected()) {
			outputs.add(output);
		}
		return outputs;
	}

	/**
	 * Returns the connection of the port. Input ports can only have one
	 * connection at max.
	 *
	 * @return the output port connected to this input port, or null.
	 */
	public OutputPort<T> connection() {
		return output;
	}

	/**
	 * Returns the value on the port.
	 *
	 * @return the value on the port, or null.
	 */
	public T getValue() {
		if (this.connection() == null) {
			return null;
		}

		return this.connection().getOutput();
	}

	// output-port signals once value/payload is ready, or reset
	protected void setReady(boolean ready) {
		setPortState(ready ? State.READY : State.WAITING);
		onValueChanged();
	}

	// lazily initialized since only usefull for runnable processors
	protected BooleanProperty valueChangedProperty;

	// flips the value changed property's boolean; only if it got instantiated
	protected void onValueChanged() {
		if (valueChangedProperty == null) {
			return;
		}
		valueChangedProperty.set(!valueChangedProperty.get());
	}

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

	@Override
	@SuppressWarnings("unchecked")
	public void connectTo(Port<?> port) {
		if (port instanceof OutputPort) {
			this.output = (OutputPort<T>) port;
			setPortState(State.WAITING);
			// always bind bi-directional, but prevent stack overflow ;)
			if (!this.output.connections().contains(this)) {
				this.output.connectTo(this);
			}
		}
	}

	@Override
	public void disconnect(Port<?> port) {
		setPortState(State.UNCONNECTED);
		// check that port is indeed this.output ommited... :)
		if (this.output != null) {
			// always unbind bi-directional, but prevent stack overflow ;)
			if (output.connections().contains(this)) {
				this.output.disconnect(this);
			}
			this.output = null;
		}
	}

	@Override
	public void disconnect() {
		disconnect(this.output);
	}

}
