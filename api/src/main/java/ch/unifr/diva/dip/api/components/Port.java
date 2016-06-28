package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 * Port interface.
 * @param <T> type of the port.
 */
public interface Port<T> {

	/**
	 * Port states.
	 */
	public enum State {

		/**
		 * An UNCONNECTED port is idling. Nothing to see, nothing to do.
		 * However, note that an unconnected port might also be disabled (see
		 * disabledProperty()). Disabled ports can not be connected to.
		 */
		UNCONNECTED,
		/**
		 * A WAITING port is blocking in case of an input port, and indicates
		 * the requirement of a value/payload on an output port. Once an output
		 * port has computed its value/payload it signals all connected input
		 * ports to set them to the READY state.
		 */
		WAITING,
		/**
		 * A READY port indicates that an input port can be used for further
		 * processing, or that the value/payload of an output port is ready and
		 * can be accessed.
		 */
		READY
	}

	/**
	 * Returns the DataType of the port. Only ports with the same DataType can
	 * be connected.
	 *
	 * @return the DataType of the port.
	 */
	public DataType getDataType();

	/**
	 * The class behind the DataType. If the DataType is a collection, then the
	 * class of an item in the collection is the class returned.
	 *
	 * @return the class behind the DataType.
	 */
	public Class<T> getType();

	/**
	 * Checks whether this port is required. A required port needs to be
	 * connected in order to have a fully functional (or executable) processor.
	 *
	 * @return True if the port is required, False otherwise.
	 */
	public boolean isRequired();

	/**
	 * Property of the port state.
	 *
	 * @return property of the port state.
	 */
	public ObjectProperty<State> portStateProperty();

	/**
	 * Returns the port state.
	 *
	 * @return the port state.
	 */
	default State getPortState() {
		return portStateProperty().get();
	}

	/**
	 * Checks whether the port is connected to another port.
	 *
	 * @return True if the port is connected, False otherwise.
	 */
	public boolean isConnected();

	/**
	 * Returns a set of all ports connected to this port. While input ports can
	 * have a single connection at most, output ports may be connected to
	 * multiple input ports.
	 *
	 * @return a set of all ports connected to this port.
	 */
	public Set<? extends Port> connections();

	/**
	 * Connects a port to this port.
	 *
	 * @param port the port to connect with.
	 */
	public void connectTo(Port port);

	/**
	 * Disconnects a port from this port.
	 *
	 * @param port the port to disconnect.
	 */
	public void disconnect(Port port);

	/**
	 * Disconnects all connections to this port.
	 */
	public void disconnect();
}
