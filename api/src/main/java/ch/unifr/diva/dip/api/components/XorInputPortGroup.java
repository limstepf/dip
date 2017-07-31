package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.services.Processor;
import java.util.Map;
import javafx.beans.InvalidationListener;

/**
 * An input port group handler with XOR logic/activation. With XOR
 * logic/activation, multiple port groups get offered, but only a single one may
 * be used/connected to. Within a port group itself, XOR logic/activation may be
 * also enabled, s. t. the port group offers multilpe ports, but only a single
 * one may be actually connected to. This may be disabled (default) s. t. all
 * ports of a port group may be connected.
 *
 * <p>
 * Processors using {@code XorInputPortGroup} should:
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
public class XorInputPortGroup extends XorPortGroupBase<InputPort<?>> {

	protected final InvalidationListener portListener;

	/**
	 * Creates a new input port group handler.
	 *
	 * @param processor the parent processor.
	 */
	public XorInputPortGroup(Processor processor) {
		super(processor);
		this.portListener = (c) -> portCallback();
	}

	protected final void portCallback() {
		setCurrentGroup();
	}

	@Override
	public Map<String, InputPort<?>> ports() {
		return processor.inputs();
	}

	@Override
	public void init(ProcessorContext context) {
		super.init(context);
		portCallback();

		for (PortGroup<InputPort<?>> group : groups) {
			for (InputPort<?> port : group.ports.values()) {
				port.portStateProperty().addListener(portListener);
			}
		}
	}

	/**
	 * Checks whether this input port group handler is connected. This method
	 * should be used to overwrite the {@code isConnected} method of the parent
	 * processor.
	 *
	 * @return {@code true} if this input port group handler is connected,
	 * {@code false} otherwise.
	 */
	public boolean isConnected() {
		if (hasEnabledGroup()) {
			return isGroupConnected(getEnabledGroup());
		} else {
			for (PortGroup<InputPort<?>> group : groups) {
				if (!isGroupConnected(group)) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Checks whether a port group is connected.
	 *
	 * @param group the port group.
	 * @return {@code true} if the port group is connected, {@code false}
	 * otherwise.
	 */
	protected boolean isGroupConnected(PortGroup<InputPort<?>> group) {
		return group.isConnected();
	}

	/**
	 * Checks whether this input port group handler is waiting. This method
	 * should be used to overwrite the {@code isWaiting} method of the parent
	 * processor.
	 *
	 * @return {@code true} if the port group handler is waiting, {@code false}
	 * otherwise.
	 */
	public boolean isWaiting() {
		if (processor.isReady()) {
			return false;
		}

		if (processor.isWaitingOnInputParams()) {
			return true;
		}

		if (hasEnabledGroup()) {
			return isGroupWaiting(getEnabledGroup());
		} else {
			for (PortGroup<InputPort<?>> group : groups) {
				if (isGroupWaiting(group)) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Checks whether a port group is waiting.
	 *
	 * @param group the port group.
	 * @return {@code true} if the port group is waiting, {@code false}
	 * otherwise.
	 */
	protected boolean isGroupWaiting(PortGroup<InputPort<?>> group) {
		if (processor.isReady()) {
			return false;
		}

		for (InputPort<?> port : group.ports.values()) {
			if (!port.connection().getPortState().equals(Port.State.READY)) {
				return true;
			}
		}
		return false;
	}

}
