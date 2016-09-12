package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Abstract description of an I/O port. Ports are directly and bidirectionally
 * linked (or bound).
 *
 * <pre>
 * unconnected (-)[OUT]-               -[IN] unconnected
 * waiting     (-)[OUT]-----------------[IN] waiting
 * ready       (V)[OUT]-----------------[IN] ready
 * unconnected (V)[OUT]-               -[IN] unconnected
 * </pre>
 *
 * @param <T> type of the port.
 */
public abstract class AbstractPort<T> implements Port<T> {

	private final DataType dataType;
	private final Class<T> type;
	private final boolean required;
	private final ObjectProperty<State> portStateProperty;

	/**
	 * Creates a new port.
	 *
	 * @param dataType data type of the port.
	 * @param required flag if the port is absolutely required to work.
	 */
	public AbstractPort(DataType dataType, boolean required) {
		this.dataType = dataType;
		this.type = this.dataType.type();
		this.required = required;
		this.portStateProperty = new SimpleObjectProperty(State.UNCONNECTED);
	}

	@Override
	public DataType getDataType() {
		return this.dataType;
	}

	@Override
	public Class<T> getType() {
		return this.type;
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}

	protected void setPortState(State state) {
		portStateProperty.set(state);
	}

	@Override
	public ObjectProperty<State> portStateProperty() {
		return portStateProperty;
	}

}
