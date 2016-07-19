package ch.unifr.diva.dip.api.datastructures;

import javafx.scene.image.Image;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A container to store a JavaFX Image in a {@code Map<String, Object>} or
 * similar.
 */
@XmlRootElement(name = "image")
@XmlAccessorType(XmlAccessType.NONE)
public class ImageContainer {

	@XmlElement(name = "data")
	public final Image image;

	@SuppressWarnings("unused")
	public ImageContainer() {
		this.image = null;
	}

	/**
	 * Creates a new image container.
	 *
	 * @param image the image to be wrapped.
	 */
	public ImageContainer(Image image) {
		this.image = image;
	}

}
