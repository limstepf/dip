package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.services.Processor;
import java.util.Map;

/**
 * An output port group handler with XOR logic/activation. With XOR
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
 * <li>call {@code isReady()} method from the processors (overwritten)
 * {@code isReady()} method, in order to correctly decide when the processor is
 * ready (only ports of the enabled port group should be considered).</li>
 * </ul>
 *
 */
public class XorOutputPortGroup extends XorPortGroupBase<OutputPort<?>> {

	/**
	 * Creates a new output port group handler.
	 *
	 * @param processor the parent processor.
	 */
	public XorOutputPortGroup(Processor processor) {
		super(processor);
	}

	@Override
	public Map<String, OutputPort<?>> ports() {
		return processor.outputs();
	}

	/**
	 * Checks whether this output port handler is ready. This method should be
	 * used to overwrite the {@code isReady} method of the parent processor.
	 *
	 * @return {@code true} if the port group handler is ready, {@code false}
	 * otherwise.
	 */
	public boolean isReady() {
		if (!processor.isReadyOutputParams()) {
			return false;
		}
		if (hasEnabledGroup()) {
			if (!isGroupReady(getEnabledGroup())) {
				return false;
			}
		} else {
			for (PortGroup<OutputPort<?>> group : groups) {
				if (!isGroupReady(group)) {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	/**
	 * Checks whether a port group is ready.
	 *
	 * @param group the port group.
	 * @return {@code true} if the port group is ready, {@code false} otherwise.
	 */
	protected boolean isGroupReady(PortGroup<OutputPort<?>> group) {
		int numConnected = 0;
		int numReady = 0;
		for (OutputPort<?> port : group.ports.values()) {
			if (port.isConnected()) {
				if (!port.getPortState().equals(Port.State.READY)) {
					return false;
				}
				numConnected++;
				numReady++;
			} else {
				if (port.getPortState().equals(Port.State.READY)) {
					numReady++;
				}
			}
		}
		return !(numConnected == 0 && numReady < group.ports.size());
	}

}
