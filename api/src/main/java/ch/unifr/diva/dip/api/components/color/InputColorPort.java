
package ch.unifr.diva.dip.api.components.color;

import ch.unifr.diva.dip.api.components.InputPort;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import java.awt.image.BufferedImage;

/**
 * Wrapper class for color typed BufferdImage input ports.
 */
public class InputColorPort extends ColorPort {

	/**
	 * The input port.
	 */
	public final InputPort<BufferedImage> port;

	public InputColorPort() {
		this(null);
	}

	public InputColorPort(SimpleColorModel cm) {
		this(cm, ColorPort.getKey(cm));
	}

	public InputColorPort(SimpleColorModel cm, String key) {
		this(cm, key, false);
	}

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
