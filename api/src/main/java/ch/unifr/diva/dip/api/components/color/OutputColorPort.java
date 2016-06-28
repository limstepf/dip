
package ch.unifr.diva.dip.api.components.color;

import ch.unifr.diva.dip.api.components.OutputPort;
import ch.unifr.diva.dip.api.imaging.SimpleColorModel;
import java.awt.image.BufferedImage;

/**
 * Wrapper class for color typed BufferdImage output ports.
 */
public class OutputColorPort extends ColorPort {

	/**
	 * The output port.
	 */
	public final OutputPort<BufferedImage> port;

	public OutputColorPort() {
		this(null);
	}

	public OutputColorPort(SimpleColorModel cm) {
		this(cm, ColorPort.getKey(cm));
	}

	public OutputColorPort(SimpleColorModel cm, String key) {
		this(cm, key, false);
	}

	public OutputColorPort(SimpleColorModel cm, String key, boolean required) {
		super(cm, key);

		if (cm == null) {
			this.port = new OutputPort(
					new ch.unifr.diva.dip.api.datatypes.BufferedImage()
			);
		} else {
			this.port = new OutputPort(
					cm.dataType()
			);
		}
	}

}
