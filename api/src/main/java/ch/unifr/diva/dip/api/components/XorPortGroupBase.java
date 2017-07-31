package ch.unifr.diva.dip.api.components;

import ch.unifr.diva.dip.api.services.Processor;
import ch.unifr.diva.dip.api.utils.FxUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A port group handler with XOR logic/activation. With XOR logic/activation,
 * multiple port groups get offered, but only a single one may be used/connected
 * to. Within a port group itself, XOR logic/activation may be also enabled, s.
 * t. the port group offers multilpe ports, but only a single one may be
 * actually connected to. This may be disabled (default) s. t. all ports of a
 * port group may be connected.
 *
 * @param <T> type of the port.
 */
public abstract class XorPortGroupBase<T extends Port<?>> {

	protected final Processor processor;
	protected final List<PortGroup<T>> groups;
	protected volatile PortGroup<T> enabledGroup;
	protected boolean portInsertPositionTop;

	/**
	 * Creates a new port group handler with XOR logic/activation.
	 *
	 * @param processor the parent processor.
	 */
	public XorPortGroupBase(Processor processor) {
		this.processor = processor;
		this.groups = new ArrayList<>();
		this.enabledGroup = null;
		this.portInsertPositionTop = true;
	}

	/**
	 * Initializes this port group handler. This method should be called in the
	 * {@code init()} method of the parent processor.
	 *
	 * @param context the processor context.
	 */
	public void init(ProcessorContext context) {

	}

	protected ObjectProperty<PortGroup<T>> enabledGroupProperty;

	/**
	 * The enabled port group property. May be used to listen to port changes
	 * upon connections have been changed by the user. This property is lazily
	 * initialized, and needs to be accessed from the JavaFX application thread
	 * only.
	 *
	 * @return the enabled port group property.
	 */
	public ReadOnlyObjectProperty<PortGroup<T>> enabledGroupProperty() {
		if (enabledGroupProperty == null) {
			enabledGroupProperty = new SimpleObjectProperty<>(enabledGroup);
		}
		return enabledGroupProperty;
	}

	/**
	 * Sets the enabled port group.
	 *
	 * @param group the enabled port group, or {@code null} to enable all.
	 */
	protected void setEnabledGroup(PortGroup<T> group) {
		enabledGroup = group;
		if (enabledGroupProperty != null) {
			FxUtils.run(() -> {
				enabledGroupProperty.set(group);
			});
		}
	}

	/**
	 * Returns the enabled port group.
	 *
	 * @return the enabled port group, or {@code null} if all port groups are
	 * enabled.
	 */
	public PortGroup<T> getEnabledGroup() {
		return enabledGroup;
	}

	/**
	 * Checks whether some specific port group is (exclusively) enabled.
	 *
	 * @return {@code true} if a port group is (exclusively) enabled,
	 * {@code false} otherwise.
	 */
	public boolean hasEnabledGroup() {
		return enabledGroup != null;
	}

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

	/**
	 * Adds a port group.
	 *
	 * @param group the port group.
	 */
	public void addGroup(PortGroup<T> group) {
		groups.add(group);
	}

	/**
	 * Sets/updates the current port group. Called after connections have been
	 * changed by the user. Enables the (first) connected port group
	 * exclusively, or all port groups, if no port group is connected.
	 */
	protected void setCurrentGroup() {
		enableGroup(getConnectedGroup());
	}

	/**
	 * Enables all port groups. This method should be called in the constructor
	 * of the parent processor, in order to "publish" all available ports.
	 */
	public void enableAllGroups() {
		enableGroup(null);
	}

	/**
	 * Enables a port group.
	 *
	 * @param group the enabled port group, or {@code null} to enable all port
	 * groups.
	 */
	public void enableGroup(PortGroup<T> group) {
		removeAllPorts();

		final Map<String, T> reinsert;
		if (portInsertPositionTop) {
			reinsert = extractPorts(ports());
		} else {
			reinsert = null;
		}

		if (group == null) {
			for (PortGroup<T> g : groups) {
				putGroupPorts(g);
			}
		} else {
			if (group.isXOR() && group.hasConnection()) {
				putConnectedGroupPort(group);
			} else {
				putGroupPorts(group);
			}
		}

		if (reinsert != null) {
			for (Map.Entry<String, T> e : reinsert.entrySet()) {
				ports().put(e.getKey(), e.getValue());
			}
		}

		setEnabledGroup(group);
		processor.repaint();
	}

	/**
	 * Puts/enables all ports of a port group.
	 *
	 * @param group the port group.
	 */
	protected void putGroupPorts(PortGroup<T> group) {
		for (Map.Entry<String, T> port : group.ports.entrySet()) {
			ports().put(port.getKey(), port.getValue());
		}
	}

	/**
	 * Puts/enables the first connected port of a port group only.
	 *
	 * @param group the port group.
	 */
	protected void putConnectedGroupPort(PortGroup<T> group) {
		for (Map.Entry<String, T> port : group.ports.entrySet()) {
			if (port.getValue().isConnected()) {
				ports().put(port.getKey(), port.getValue());
				return;
			}
		}
	}

	/**
	 * Removes all ports from all port groups.
	 */
	protected void removeAllPorts() {
		for (PortGroup<T> group : groups) {
			for (String key : group.ports.keySet()) {
				ports().remove(key);
			}
		}
	}

	/**
	 * Returns the (first) connected port group.
	 *
	 * @return the (first) connected port group, or {@code null} if no port
	 * group has a connection.
	 */
	protected PortGroup<T> getConnectedGroup() {
		for (PortGroup<T> group : groups) {
			if (group.hasConnection()) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Returns the parent processor ports. These are either the input or the
	 * output ports.
	 *
	 * @return the parent processor's ports.
	 */
	public abstract Map<String, T> ports();

	/**
	 * A port group.
	 *
	 * @param <T> type of the port.
	 */
	public static class PortGroup<T extends Port<?>> {

		protected final LinkedHashMap<String, T> ports;
		boolean enableXOR;

		/**
		 * Creates a new port group (with multiple connections allowed).
		 */
		public PortGroup() {
			this(false);
		}

		/**
		 * Creates a new port group. With XOR ports enabled, only a single port
		 * from this group can be connected to, otherwise its possible to
		 * connect to all ports of the group.
		 *
		 * @param enableXOR {@code true} to enable XOR ports, {@code false} to
		 * allow multiple connections.
		 */
		public PortGroup(boolean enableXOR) {
			this.ports = new LinkedHashMap<>();
			this.enableXOR = enableXOR;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName()
					+ "@"
					+ Integer.toHexString(hashCode())
					+ "{"
					+ "ports=" + ports.keySet()
					+ ", xor=" + enableXOR
					+ "}";
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
		 * Enables/disables XOR ports.
		 *
		 * @param enable {@code true} to enable XOR ports, {@code false} to
		 * allow multiple connections.
		 */
		public void setXOR(boolean enable) {
			enableXOR = enable;
		}

		/**
		 * Checks whether XOR ports are enabled. If XOR ports are enabled, only
		 * a single port from this group can be connected to, otherwise its
		 * possible to connect to all ports of the group.
		 *
		 * @return {@code true} if XOR ports are enabled, {@code false} if
		 * multiple connections are allowed.
		 */
		public boolean isXOR() {
			return enableXOR;
		}

		/**
		 * Checks whether this port group has at least one connection.
		 *
		 * @return {@code true} if at least one port of this port group is
		 * connected, {@code false} otherwise.
		 */
		protected boolean hasConnection() {
			for (T port : ports.values()) {
				if (port.isConnected()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Checks whether this port group is considered (fully) connected. Two
		 * cases: with required ports all required ports need to be connected,
		 * without required ports at least one has to be connected.
		 *
		 * @return {@code true} if this port groupr is (fully) connected,
		 * {@code false} otherwise.
		 */
		protected boolean isConnected() {
			int connected = 0;
			for (T port : ports.values()) {
				if (port.isRequired()) {
					if (!port.isConnected()) {
						return false;
					}
					connected++;
				} else {
					if (port.isConnected()) {
						connected++;
					}
				}
			}
			return connected > 0;
		}

		/**
		 * Returns the (first) connected port of this port group.
		 *
		 * @return the (first) connected port, or {@code null} if no port is
		 * connected.
		 */
		public T getConnection() {
			for (T port : ports.values()) {
				if (port.isConnected()) {
					return port;
				}
			}
			return null;
		}

	}

}
