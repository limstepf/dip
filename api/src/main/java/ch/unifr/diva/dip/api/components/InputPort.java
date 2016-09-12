package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import java.util.HashSet;
import java.util.Set;

/**
 * Input port. Input ports provide objects to DIP processors.
 *
 * @param <T> type of the port.
 */
public class InputPort<T> extends AbstractPort<T> {

	private OutputPort output;

	/**
	 * Creates a new input port.
	 *
	 * @param dataType data type of the port.
	 * @param required flag if the port is absolutely required to work.
	 */
	public InputPort(DataType dataType, boolean required) {
		super(dataType, required);
		setPortState(State.UNCONNECTED);
	}

	@Override
	public boolean isConnected() {
		return !getPortState().equals(State.UNCONNECTED);
	}

	@Override
	public Set<OutputPort> connections() {
		final Set<OutputPort> outputs = new HashSet<>();
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
	public OutputPort connection() {
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

		return (T) this.connection().getOutput();
	}

	// output-port signals once value/payload is ready, or reset
	protected void setReady(boolean ready) {
		setPortState(ready ? State.READY : State.WAITING);
	}

	@Override
	public void connectTo(Port port) {
		if (port instanceof OutputPort) {
			this.output = (OutputPort) port;
			setPortState(State.WAITING);
			// always bind bi-directional, but prevent stack overflow ;)
			if (!this.output.connections().contains(this)) {
				this.output.connectTo(this);
			}
		}
	}

	@Override
	public void disconnect(Port port) {
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
