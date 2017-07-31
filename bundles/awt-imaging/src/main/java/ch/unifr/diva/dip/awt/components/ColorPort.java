package ch.unifr.diva.dip.awt.components;

import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import java.util.List;

/**
 * Wrapper class for color typed BufferdImage ports.
 */
public class ColorPort {

	/**
	 * The associated color model. Can be {@code null} for a color untyped port.
	 */
	public final SimpleColorModel cm;

	/**
	 * Port key.
	 */
	public final String key;

	/**
	 * Creates a new color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 */
	public ColorPort(SimpleColorModel cm) {
		this(cm, getKey(cm));
	}

	/**
	 * Creates a new color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 * @param key the key of the port.
	 */
	public ColorPort(SimpleColorModel cm, String key) {
		this.cm = cm;
		this.key = key;
	}

	/**
	 * Returns the default key of a color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 * @return the default key of the color port.
	 */
	protected static String getKey(SimpleColorModel cm) {
		if (cm != null) {
			return cm.name().toLowerCase();
		}

		return "buffered-image"; // for a color untyped port
	}

	/**
	 * Searches a color port by name of the color model.
	 *
	 * @param <T> subclass of ColorPort.
	 * @param name name of a color model.
	 * @param ports a list of ports to search for the port.
	 * @return a color port. Returns the first port in the given list of ports
	 * if no port is found by the given name
	 */
	public static <T extends ColorPort> T getColorPort(String name, List<T> ports) {
		return getColorPort(name, ports, ports.get(0));
	}

	/**
	 * Searches a color port by name of the color model.
	 *
	 * @param <T> subclass of ColorPort.
	 * @param name name of a color model.
	 * @param ports a list of ports to search for the port.
	 * @param defaultPort default port returned if no port is found by the given
	 * name.
	 * @return a color port.
	 */
	public static <T extends ColorPort> T getColorPort(String name, List<T> ports, T defaultPort) {
		for (T port : ports) {
			if (port.cm.name().equals(name)) {
				return port;
			}
		}

		return defaultPort;
	}

}
