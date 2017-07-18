package ch.unifr.diva.dip.api.utils.jaxb;

import javafx.geometry.Rectangle2D;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFX Rectangle2D adapter.
 */
public class Rectangle2DAdapter extends XmlAdapter<ch.unifr.diva.dip.api.datastructures.Rectangle2D, Rectangle2D> {

	@Override
	public Rectangle2D unmarshal(ch.unifr.diva.dip.api.datastructures.Rectangle2D vt) throws Exception {
		return vt.toFXRectangle2D();
	}

	@Override
	public ch.unifr.diva.dip.api.datastructures.Rectangle2D marshal(Rectangle2D bt) throws Exception {
		return new ch.unifr.diva.dip.api.datastructures.Rectangle2D(bt);
	}

}
