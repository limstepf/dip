package ch.unifr.diva.dip.awt.components;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import java.awt.image.BufferedImage;

/**
 * Wrapper class for color typed BufferdImage input ports.
 */
public class InputColorPort extends ColorPort {

	/**
	 * The input port.
	 */
	public final InputPort<BufferedImage> port;

	/**
	 * Creates a new input color port.
	 */
	public InputColorPort() {
		this(null);
	}

	/**
	 * Creates a new input color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 */
	public InputColorPort(SimpleColorModel cm) {
		this(cm, ColorPort.getKey(cm));
	}

	/**
	 * Creates a new input color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 * @param key the key of the port.
	 */
	public InputColorPort(SimpleColorModel cm, String key) {
		this(cm, key, false);
	}

	/**
	 * Creates a new input color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 * @param key the key of the port.
	 * @param required whether nor not this is a required port.
	 */
	public InputColorPort(SimpleColorModel cm, String key, boolean required) {
		super(cm, key);

		if (cm == null) {
			this.port = new InputPort(
					new ch.unifr.diva.dip.api.datatypes.BufferedImage(),
					required
			);
		} else {
			this.port = new InputPort(
					cm.dataType(),
					required
			);
		}
	}

}
