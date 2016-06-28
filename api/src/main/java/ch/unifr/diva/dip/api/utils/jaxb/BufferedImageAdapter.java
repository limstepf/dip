
package ch.unifr.diva.dip.api.utils.jaxb;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * BufferedImage adapter for JAXB.
 * Stores the image in Base64.
 */
public class BufferedImageAdapter extends XmlAdapter<Image,BufferedImage> {

	@Override
	public BufferedImage unmarshal(Image value) throws Exception {
		return (BufferedImage) value;
	}

	@Override
	public Image marshal(BufferedImage image) throws Exception {
		return (Image) image;
	}

}
