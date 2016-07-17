
package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @param <T>
 */
public class InputPort<T> extends AbstractPort<T> {

	private OutputPort output;

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

	public OutputPort connection() {
		return output;
	}

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
