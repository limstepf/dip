package ch.unifr.diva.dip.api.utils.jaxb;

import ch.unifr.diva.dip.api.datastructures.FxColor;
import javafx.scene.paint.Color;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFX color adapter for JAXB.
 */
public class ColorAdapter extends XmlAdapter<FxColor, javafx.scene.paint.Color> {

	@Override
	public Color unmarshal(FxColor vt) throws Exception {
		return vt.toColor();
	}

	@Override
	public FxColor marshal(Color bt) throws Exception {
		return new FxColor(bt);
	}

}
