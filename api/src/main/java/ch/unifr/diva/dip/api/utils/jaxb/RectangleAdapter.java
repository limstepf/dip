package ch.unifr.diva.dip.api.utils.jaxb;

import ch.unifr.diva.dip.api.datastructures.IntegerRectangle;
import java.awt.Rectangle;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * AWT Rectangle adapter.
 */
public class RectangleAdapter extends XmlAdapter<IntegerRectangle, Rectangle> {

	@Override
	public Rectangle unmarshal(IntegerRectangle vt) throws Exception {
		return vt.toAWTRectangle();
	}

	@Override
	public IntegerRectangle marshal(Rectangle bt) throws Exception {
		return new IntegerRectangle(bt);
	}

}
