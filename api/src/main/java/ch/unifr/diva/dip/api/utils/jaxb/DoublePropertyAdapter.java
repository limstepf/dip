package ch.unifr.diva.dip.api.utils.jaxb;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JavaFx DoubleProperty (XML-)adapter for JAXB.
 */
public class DoublePropertyAdapter extends XmlAdapter<Double,DoubleProperty> {

	@Override
	public DoubleProperty unmarshal(Double value) throws Exception {
		return new SimpleDoubleProperty(value);
	}

	@Override
	public Double marshal(DoubleProperty property) throws Exception {
		return property.doubleValue();
	}

}
