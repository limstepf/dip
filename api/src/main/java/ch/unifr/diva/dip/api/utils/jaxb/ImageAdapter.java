package ch.unifr.diva.dip.api.utils.jaxb;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFX image adapter for JAXB. Converts to awt.image and stores it in Base64.
 */
public class ImageAdapter extends XmlAdapter<Image, javafx.scene.image.Image> {

	@Override
	public javafx.scene.image.Image unmarshal(Image value) throws Exception {
		return SwingFXUtils.toFXImage((BufferedImage) value, null);
	}

	@Override
	public Image marshal(javafx.scene.image.Image image) throws Exception {
		return (Image) SwingFXUtils.fromFXImage(image, null);
	}

}
