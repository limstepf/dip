package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.services.Processor;
import java.util.Map;
import javafx.beans.InvalidationListener;

/**
 * An input port handler with XOR logic/activation. With XOR logic/activation,
 * multiple ports get offered, but only a single one may be connected to.
 *
 * <p>
 * Processors using {@code XorInputPorts} should:
 *
 * <ul>
 * <li>enable (or "publish") all ports in the constructor of the processor by a
 * call to {@code enableAllPorts()}.</li>
 *
 * <li>call {@code init(ProcessorContext context)} method from the processor's
 * {@code init(ProcessorContext context)} method, no matter if
 * {@code ProcessorContext} is {@code null} or not (i.e. in any case), in order
 * to attach port listeners, and enable/disable ports.</li>
 * </ul>
 *
 */
public class XorInputPorts extends XorPortsBase<InputPort<?>> {

	protected final InvalidationListener portListener;

	/**
	 * Creates a new input port handler with XOR logic/activation.
	 *
	 * @param processor the parent processor.
	 */
	public XorInputPorts(Processor processor) {
		super(processor);
		this.portListener = (c) -> portCallback();
	}

	protected final void portCallback() {
		InputPort<?> connectedPort = null;
		for (InputPort<?> port : ports.values()) {
			if (port.isConnected()) {
				connectedPort = port;
				break;
			}
		}
		enablePort(connectedPort);
	}

	/**
	 * Checks whether this input port handler is connected. This method should
	 * be used to overwrite the {@code isConnected} method of the parent
	 * processor.
	 *
	 * @return {@code true} if this input port handler is connected,
	 * {@code false} otherwise.
	 */
	public boolean isConnected() {
		for (InputPort<?> port : ports.values()) {
			if (port.isConnected()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Map<String, InputPort<?>> ports() {
		return processor.inputs();
	}

	@Override
	public void init(ProcessorContext context) {
		super.init(context);
		portCallback();

		for (InputPort<?> port : ports.values()) {
			port.portStateProperty().addListener(portListener);
		}
	}

}
