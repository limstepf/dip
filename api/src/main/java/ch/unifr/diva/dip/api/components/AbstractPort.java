package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.datatypes.DataType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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

	private final DataType<T> dataType;
	private final Class<T> type;
	private final boolean required;
	private final ObjectProperty<State> portStateProperty;
	private final String label;

	/**
	 * Creates a new port.
	 *
	 * @param label a custom port label (short and descriptive), or an empty
	 * string for a default label.
	 * @param dataType data type of the port.
	 * @param required flag if the port is absolutely required to work.
	 */
	public AbstractPort(String label, DataType<T> dataType, boolean required) {
		this.dataType = dataType;
		this.type = this.dataType.type();
		this.required = required;
		this.label = makeLabel(dataType, label);
		this.portStateProperty = new SimpleObjectProperty<>(State.UNCONNECTED);
	}

	private static <T> String makeLabel(DataType<T> dataType, String label) {
		if (label == null || label.isEmpty()) {
			if (!dataType.label().isEmpty()) {
				return dataType.label();
			}
			return dataType.getClass().getSimpleName();
		}
		return label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public DataType<T> getDataType() {
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

	@Override
	public ReadOnlyObjectProperty<State> portStateProperty() {
		return portStateProperty;
	}

	/**
	 * Sets/udpates the port state. This method is only exposed to the package,
	 * and to be used by the direct subclasses {@code InputPort} and
	 * {@code OutputPort}, for otherwise the state property is supposed to be
	 * read only.
	 *
	 * @param state the new state of the port.
	 */
	protected void setPortState(State state) {
		portStateProperty.set(state);
	}

}
