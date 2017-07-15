package ch.unifr.diva.dip.awt.components;

import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.awt.imaging.SimpleColorModel;
import java.awt.image.BufferedImage;

/**
 * Wrapper class for color typed BufferdImage output ports.
 */
public class OutputColorPort extends ColorPort {

	/**
	 * The output port.
	 */
	public final OutputPort<BufferedImage> port;

	/**
	 * Creates a new output color port.
	 */
	public OutputColorPort() {
		this(null);
	}

	/**
	 * Creates a new output color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 */
	public OutputColorPort(SimpleColorModel cm) {
		this(cm, ColorPort.getKey(cm));
	}

	/**
	 * Creates a new output color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 * @param key the key of the port.
	 */
	public OutputColorPort(SimpleColorModel cm, String key) {
		this(cm, key, false);
	}

	/**
	 * Creates a new output color port.
	 *
	 * @param cm the color model specifying the type of this port.
	 * @param key the key of the port.
	 * @param required whether nor not this is a required port.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public OutputColorPort(SimpleColorModel cm, String key, boolean required) {
		super(cm, key);

		if (cm == null) {
			this.port = new OutputPort<>(
					new ch.unifr.diva.dip.api.datatypes.BufferedImage()
			);
		} else {
			this.port = new OutputPort(
					cm.dataType()
			);
		}
	}

}
