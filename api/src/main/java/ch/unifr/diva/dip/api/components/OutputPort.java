
package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @param <T>
 */
public class OutputPort<T> extends AbstractPort<T> {

	private volatile T value;
	private final Set<InputPort> inputs = new HashSet<>();

	public OutputPort(DataType dataType) {
		super(dataType, false);
	}

	public void setOutput(T value) {
		this.value = value;

		if (hasValue()) {
			setPortState(State.READY);
			signalInputs();
		} else {
			setPortState(getIdleState());
		}
	}

	private boolean hasValue() {
		return (this.value != null);
	}

	private void signalInputs() {
		for (InputPort input : inputs) {
			input.setReady();
		}
	}

	private State getIdleState() {
		return isConnected() ? State.WAITING : State.UNCONNECTED;
	}

	public T getOutput() {
		return (T) value;
	}

	@Override
	public boolean isConnected() {
		return !inputs.isEmpty();
	}

	@Override
	public Set<InputPort> connections() {
		return inputs;
	}

	@Override
	public void connectTo(Port port) {
		if (port instanceof InputPort) {
			InputPort input = (InputPort) port;
			// always bind bi-directional, but prevent stack overflow ;)
			if (!input.isConnected() && !input.connection().equals(this)) {
				input.connectTo(this);
			}
			inputs.add(input);
			setPortState(hasValue() ? State.READY : State.WAITING);
			if (getPortState().equals(State.READY)) {
				signalInputs();
			}
		}
	}

	@Override
	public void disconnect(Port port) {
		if (port instanceof InputPort) {
			final InputPort input = (InputPort) port;
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
		for (InputPort input : inputs) {
			disconnect(input);
		}
	}

}
