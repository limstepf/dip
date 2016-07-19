package ch.unifr.diva.dip.api.datastructures;

import java.awt.image.BufferedImage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A container to store a BufferedImage in a {@code Map<String, Object>} or
 * similar.
 */
@XmlRootElement(name = "buffered-image")
@XmlAccessorType(XmlAccessType.NONE)
public class BufferedImageContainer {

	@XmlElement(name = "data")
	public final BufferedImage image;

	@SuppressWarnings("unused")
	public BufferedImageContainer() {
		this.image = null;
	}

	/**
	 * Creates a new buffered image container.
	 *
	 * @param image the image to wrap.
	 */
	public BufferedImageContainer(BufferedImage image) {
		this.image = image;
	}

}
