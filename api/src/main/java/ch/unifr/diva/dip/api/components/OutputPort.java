package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import java.util.HashSet;
import java.util.Set;

/**
 * Output port. DIP processors provide objects to output ports.
 *
 * @param <T> type of the value on the port (i.e. not the DataType).
 */
public class OutputPort<T> extends AbstractPort<T> {

	private volatile T value;
	private final Set<InputPort<?>> inputs = new HashSet<>();

	/**
	 * Creates a new output port.
	 *
	 * @param dataType the data type of the port.
	 */
	public OutputPort(DataType<T> dataType) {
		super(dataType, false);
	}

	/**
	 * Sets/updates the value on the output port.
	 *
	 * @param value the new value on the port.
	 */
	public void setOutput(T value) {
		this.value = value;

		if (hasValue()) {
			setPortState(State.READY);
			signalInputs(true);
		} else {
			setPortState(getIdleState());
			signalInputs(false);
		}
	}

	private boolean hasValue() {
		return (this.value != null);
	}

	private void signalInputs(boolean ready) {
		for (InputPort<?> input : inputs) {
			input.setReady(ready);
		}
	}

	private State getIdleState() {
		return isConnected() ? State.WAITING : State.UNCONNECTED;
	}

	public T getOutput() {
		return value;
	}

	@Override
	public boolean isConnected() {
		return !inputs.isEmpty();
	}

	@Override
	public Set<InputPort<?>> connections() {
		return inputs;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void connectTo(Port<?> port) {
		if (port instanceof InputPort) {
			InputPort<T> input = (InputPort<T>) port;
			// always bind bi-directional, but prevent stack overflow ;)
			if (!input.isConnected() && !input.connection().equals(this)) {
				input.connectTo(this);
			}
			inputs.add(input);
			setPortState(hasValue() ? State.READY : State.WAITING);
			if (getPortState().equals(State.READY)) {
				signalInputs(true);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void disconnect(Port<?> port) {
		if (port instanceof InputPort) {
			final InputPort<T> input = (InputPort<T>) port;
			// always unbind bi-directional, but prevent stack overflow ;)
			if (input.isConnected() && input.connection().equals(this)) {
				input.disconnect(this);
			}
			inputs.remove(input);
		}

		if (inputs.isEmpty()) {
			setPortState(State.UNCONNECTED);
		}
	}

	@Override
	public void disconnect() {
		for (InputPort<?> input : inputs) {
			disconnect(input);
		}
	}

}
