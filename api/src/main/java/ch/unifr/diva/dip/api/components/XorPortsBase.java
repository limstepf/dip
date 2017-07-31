package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.FxUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A port handler with XOR logic/activation. With XOR logic/activation, multiple
 * ports get offered, but only a single one may be connected to.
 *
 * @param <T> type of the port.
 */
public abstract class XorPortsBase<T extends Port<?>> {

	protected final Processor processor;
	protected final Map<String, T> ports;
	protected volatile T enabledPort;
	protected boolean portInsertPositionTop;

	/**
	 * Creates new port handler with XOR logic/activation.
	 *
	 * @param processor the parent processor.
	 */
	public XorPortsBase(Processor processor) {
		this.processor = processor;
		this.ports = new LinkedHashMap<>();
		this.enabledPort = null;
		this.portInsertPositionTop = true;
	}

	/**
	 * Initializes this port handler. This method should be called in the
	 * {@code init()} method of the parent processor.
	 *
	 * @param context the processor context.
	 */
	public void init(ProcessorContext context) {

	}

	protected ObjectProperty<T> enabledPortProperty;

	/**
	 * The enabled port property. May be used to listen to port changes upon
	 * connections have been changed by the user. This property is lazily
	 * initialized, and needs to be accessed from the JavaFX application thread
	 * only.
	 *
	 * @return the enabled port property.
	 */
	public ReadOnlyObjectProperty<T> enabledPortProperty() {
		if (enabledPortProperty == null) {
			enabledPortProperty = new SimpleObjectProperty<>(enabledPort);
		}
		return enabledPortProperty;
	}

	/**
	 * Sets the enabled port.
	 *
	 * @param port the enabled port, or {@code null} to enable all ports.
	 */
	protected void setEnabledPort(T port) {
		enabledPort = port;
		if (enabledPortProperty != null) {
			FxUtils.run(() -> {
				enabledPortProperty.set(port);
			});
		}
	}

	/**
	 * Returns the enabled port.
	 *
	 * @return the enabled port, or {@code null} if all ports are enabled.
	 */
	public T getEnabledPort() {
		return enabledPort;
	}

	/**
	 * Checks whether some specific port is (exclusively) enabled.
	 *
	 * @return {@code true} if a port is (exclusively) enabled, {@code false}
	 * otherwise.
	 */
	public boolean hasEnabledPort() {
		return enabledPort != null;
	}

	/**
	 * Adds a port.
	 *
	 * @param key the key of the port.
	 * @param port the port.
	 */
	public void addPort(String key, T port) {
		ports.put(key, port);
	}

	/**
	 * Enables all ports. This method should be called in the constructor of the
	 * parent processor, in order to "publish" all available ports.
	 */
	public void enableAllPorts() {
		enablePorts(null);
	}

	/**
	 * Enables a specific port.
	 *
	 * @param key the key of the port.
	 */
	public void enablePort(String key) {
		enablePorts(ports.get(key));
	}

	/**
	 * Enables a specific port.
	 *
	 * @param port the port.
	 */
	public void enablePort(T port) {
		enablePorts(port);
	}

	/**
	 * Enables port(s).
	 *
	 * @param port a port to enable, or {@code null} to enable all.
	 */
	protected void enablePorts(T port) {
		removeAllPorts();

		final Map<String, T> reinsert;
		if (portInsertPositionTop) {
			reinsert = extractPorts(ports());
		} else {
			reinsert = null;
		}

		for (Map.Entry<String, T> e : ports.entrySet()) {
			if (port == null || e.getValue().equals(port)) {
				ports().put(e.getKey(), e.getValue());
			}
		}

		if (reinsert != null) {
			for (Map.Entry<String, T> e : reinsert.entrySet()) {
				ports().put(e.getKey(), e.getValue());
			}
		}

		setEnabledPort(port);
		processor.repaint();
	}

	/**
	 * Removes all ports from the processor.
	 */
	protected void removeAllPorts() {
		for (String key : ports.keySet()) {
			ports().remove(key);
		}
	}

	/**
	 * Returns the parent processor ports. These are either the input or the
	 * output ports.
	 *
	 * @return the parent processor's ports.
	 */
	public abstract Map<String, T> ports();

	/**
	 * Sets the port insert position. Under the assumption that ports are kept
	 * in a linked hash map, we may (re-)insert the ports of this unit either at
	 * the top (default), or at the bottom.
	 *
	 * @param top ports of the unit are (re-)inserted at the top if
	 * {@code true}, at the bottom otherwise.
	 */
	public void setPortInsertPosition(boolean top) {
		portInsertPositionTop = top;
	}

	/**
	 * Extracts all (remaining) ports on a processor.
	 *
	 * @param <T> the type of the ports.
	 * @param values the port map (will be cleared/empty after a call to this
	 * method).
	 * @return a new map with the extracted (remaining) ports.
	 */
	protected static <T> Map<String, T> extractPorts(Map<String, T> values) {
		final Map<String, T> m = new LinkedHashMap<>();
		for (Map.Entry<String, T> e : values.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
		values.clear();
		return m;
	}

}
